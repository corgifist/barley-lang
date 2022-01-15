package com.barley.runtime;

import com.barley.utils.BarleyException;
import com.barley.utils.Function;
import okhttp3.*;
import okhttp3.internal.http.HttpMethod;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public final class BarleyHTTP implements Function {

    private static final BarleyValue
            HEADER_KEY = new BarleyString("header"),
            CHARSET_KEY = new BarleyString("charset"),
            ENCODED_KEY = new BarleyString("encoded"),
            CONTENT_TYPE = new BarleyString("content_type"),
            EXTENDED_RESULT = new BarleyString("extended_result");

    private static final MediaType URLENCODED_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public BarleyValue execute(BarleyValue... args) {
        String url, method;
        switch (args.length) {
            case 1: // http(url)
                url = args[0].toString();
                return process(url, "GET");

            case 2: // http(url, method) || http(url, callback)
                url = args[0].toString();
                if (args[1] instanceof BarleyFunction) {
                    return process(url, "GET", (BarleyFunction) args[1]);
                }
                return process(url, args[1].toString());

            case 3: // http(url, method, params) || http(url, method, callback)
                url = args[0].toString();
                method = args[1].toString();
                if (args[2] instanceof BarleyFunction) {
                    return process(url, method, (BarleyFunction) args[2]);
                }
                return process(url, method, args[2], BarleyFunction.EMPTY);

            case 4: // http(url, method, params, callback)
                if (args[3] instanceof BarleyFunction) {
                    throw new BarleyException("BadHTTP", "Fourth arg must be a function callback");
                }
                url = args[0].toString();
                method = args[1].toString();
                return process(url, method, args[2], (BarleyFunction) args[3]);

            case 5: // http(url, method, params, headerParams, callback)
                if (args[3] instanceof BarleyReference){
                    throw new BarleyException("BadHTTP", "Expected map as params");
                }
                if (args[4] instanceof BarleyFunction) {
                    throw new BarleyException("BadHTTP", "Fifth arg must be a function callback");
                }
                url = args[0].toString();
                method = args[1].toString();
                return process(url, method, args[2], (BarleyReference) args[3], (BarleyFunction) args[4]);

            default:
                throw new BarleyException("BadHTTP", "Argument mismatch");
        }
    }

    private BarleyValue process(String url, String method) {
        return process(url, method, BarleyFunction.EMPTY);
    }

    private BarleyValue process(String url, String method, BarleyFunction function) {
        return process(url, method, new BarleyReference(new HashMap<BarleyValue, BarleyValue>()), function);
    }

    private BarleyValue process(String url, String method, BarleyValue params, BarleyFunction function) {
        return process(url, method, params, new BarleyReference(new HashMap<BarleyValue, BarleyValue>()), function);
    }

    private BarleyValue process(String url, String methodStr, BarleyValue requestParams, BarleyReference opts, BarleyFunction function) {
        final String method = methodStr.toUpperCase();
        final Function callback = function.getFunction();
        HashMap<BarleyValue, BarleyValue> options = (HashMap<BarleyValue, BarleyValue>) opts.getRef();
        try {
            final Request.Builder builder = new Request.Builder()
                    .url(url)
                    .method(method, getRequestBody(method, requestParams, opts));

            final Response response = client.newCall(builder.build()).execute();
            callback.execute(getResult(response, opts));
            return new BarleyNumber(response.isSuccessful() ? 1 : 0);
        } catch (IOException ex) {
            return new BarleyNumber(0);
        }
    }

    private BarleyValue getResult(Response response, BarleyReference opts) throws IOException {
        HashMap<BarleyValue, BarleyValue> options = (HashMap<BarleyValue, BarleyValue>) opts.getRef();
        if (options.containsKey(EXTENDED_RESULT)) {
            final HashMap<BarleyValue, BarleyValue> map = new HashMap<>();
            map.put(new BarleyString("text"), new BarleyString(response.body().string()));
            map.put(new BarleyString("message"), new BarleyString(response.message()));
            map.put(new BarleyString("code"), new BarleyNumber(response.code()));
            map.put(new BarleyString("length"), new BarleyNumber(response.body().contentLength()));
            map.put(CONTENT_TYPE, new BarleyString(response.body().contentType().toString()));
            return new BarleyReference(map);
        }
        return new BarleyString(response.body().string());
    }

    private void applyHeaderParams(BarleyReference h, Request.Builder builder) {
        HashMap<BarleyValue, BarleyValue> headerParams = (HashMap<BarleyValue, BarleyValue>) h.getRef();
        for (Map.Entry<BarleyValue, BarleyValue> p : headerParams.entrySet()) {
            builder.header(p.getKey().toString(), p.getValue().toString());
        }
    }

    private RequestBody getRequestBody(String method, BarleyValue params, BarleyReference options) throws UnsupportedEncodingException {
        if (!HttpMethod.permitsRequestBody(method)) return null;

        if (params instanceof BarleyReference) {
            return getMapRequestBody((BarleyReference) params, options);
        }
        return getStringRequestBody(params, (BarleyReference) options);
    }

    private RequestBody getMapRequestBody(BarleyReference prs, BarleyReference opts) {
        HashMap<BarleyValue, BarleyValue> params = (HashMap<BarleyValue, BarleyValue>) prs.getRef();
        HashMap<BarleyValue, BarleyValue> options = (HashMap<BarleyValue, BarleyValue>) opts.getRef();
        final FormBody.Builder form = new FormBody.Builder();
        final boolean alreadyEncoded = (options.containsKey(ENCODED_KEY)
                && options.get(ENCODED_KEY).asInteger().intValue() != 0);
        for (HashMap.Entry<BarleyValue, BarleyValue> param : params.entrySet()) {
            final String name = param.getKey().toString();
            final String value = param.getValue().toString();
            if (alreadyEncoded)
                form.addEncoded(name, value);
            else
                form.add(name, value);
        }
        return form.build();
    }

    private RequestBody getStringRequestBody(BarleyValue params, BarleyReference opts) throws UnsupportedEncodingException {
        final MediaType type;
        HashMap<BarleyValue, BarleyValue> options = (HashMap<BarleyValue, BarleyValue>) opts.getRef();
        if (options.containsKey(CONTENT_TYPE)) {
            type = MediaType.parse(options.get(CONTENT_TYPE).toString());
        } else {
            type = URLENCODED_MEDIA_TYPE;
        }

        if (options.containsKey(CHARSET_KEY)) {
            final String charset = options.get(CHARSET_KEY).toString();
            return RequestBody.create(type, params.toString().getBytes(charset));
        }

        return RequestBody.create(type, params.toString());
    }
}
