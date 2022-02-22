package com.jkotima.seinahullu.payload.request;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequest {
  @NotBlank
  private String content;
}
