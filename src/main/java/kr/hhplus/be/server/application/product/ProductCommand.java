package kr.hhplus.be.server.application.product;

public record ProductCommand() {

    public record ListQuery(int offset, int limit) {}

}
