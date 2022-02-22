package com.jkotima.seinahullu.payload.request;

import javax.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class ReplaceUserRequest {
  @NotBlank
  @Size(min = 3, max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;

  private Set<String> roles;
}
