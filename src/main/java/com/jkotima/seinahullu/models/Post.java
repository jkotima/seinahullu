package com.jkotima.seinahullu.models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.springframework.data.jpa.domain.AbstractPersistable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import javax.persistence.*;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post extends AbstractPersistable<Long> {
  @Temporal(TemporalType.TIMESTAMP)
  private Date creationDateTime;
  private String content;

  @ManyToOne
  private User user;
}
