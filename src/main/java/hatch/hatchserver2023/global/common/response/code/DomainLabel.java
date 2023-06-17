package hatch.hatchserver2023.global.common.response.code;

import lombok.Getter;

//@Getter
public enum DomainLabel {
    COMMON("COMMON", "공통"),
    USER("USER", "사용자"),
    ;
    private final String initial;
    private final String label;

    DomainLabel(String initial, String label) {
        this.initial = initial;
        this.label = label;
    }

    public String getInitial() {
        return this.initial + "-";
    }

    public String getLabel() {
        return "[" + this.label + "] ";
    }
}
