package com.jkotima.seinahullu.models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.domain.AbstractPersistable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post extends AbstractPersistable<Long> {
  @Temporal(TemporalType.TIMESTAMP)
  private Date creationDateTime;
  private String content;

  @JsonIgnore
  @ManyToOne
  private User user;
}
