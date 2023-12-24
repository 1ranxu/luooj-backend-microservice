package com.luoying.luoojbackendmodel.codesanbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeRequest {
    private String input;

    private String code;

    private String language;
}
