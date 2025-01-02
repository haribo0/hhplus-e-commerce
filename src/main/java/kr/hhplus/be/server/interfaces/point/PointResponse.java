package kr.hhplus.be.server.interfaces.point;

public record PointResponse() {

    public record charge(String msg, int amount){

    }
    public record view(int amount){

    }
}
