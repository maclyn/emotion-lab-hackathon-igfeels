package com.hackathon.igfeels.instagramApi;

public class UserQueryResult {
    public class Meta {
        private int code;

        public Meta(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private Meta meta;
    private UserEntry[] data;

    public UserQueryResult(Meta meta, UserEntry[] data) {
        this.meta = meta;
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public UserEntry[] getData() {
        return data;
    }
}
