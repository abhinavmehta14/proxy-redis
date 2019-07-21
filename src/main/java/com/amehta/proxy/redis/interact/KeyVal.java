package com.amehta.proxy.redis.interact;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

public class KeyVal {
    private String key;

    @Length(max = 3)
    private String value;

    public KeyVal() {
        // Jackson deserialization
    }

    public KeyVal(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public String getValue() {
        return value;
    }
}
