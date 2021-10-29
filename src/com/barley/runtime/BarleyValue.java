package com.barley.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface BarleyValue {
    BigInteger asInteger();

    BigDecimal asFloat();

}
