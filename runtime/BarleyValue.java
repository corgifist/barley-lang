package com.barley.runtime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface BarleyValue extends Serializable {
    BigInteger asInteger();

    BigDecimal asFloat();

}
