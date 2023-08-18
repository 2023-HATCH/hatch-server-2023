package hatch.hatchserver2023.global.config.socket;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final String name; //소켓 통신 시 해당 세션(연결)에 붙는 식별값

    //nickname, userId 등을 더 저장해둘 수도 있음

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
