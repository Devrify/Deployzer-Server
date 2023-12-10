package com.devrify.deployzerserver.common.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DeployzerPlaceHolderEnum {

    PLACE_HOLDER("${}"),

    ;

    private final String placeHolderValue;
}
