package com.fct.geojebus.util;

public class HttpJSONQuery {

    public HttpJSONQuery() {

    }

    public HttpJSONQuery ajax(HttpJSONQueryCallback callback) {
        return invoke(callback);
    }

    public HttpJSONQuery ajax(String url, HttpJSONQueryCallback callback) {
        callback.url(url);

        return ajax(callback);
    }

    protected HttpJSONQuery invoke(HttpJSONQueryCallback<?> callback) {
        callback.async();

        return this;
    }

}
