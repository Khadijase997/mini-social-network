package com.connecthub.socialnetwork.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;

@Node
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    private String content;
    private LocalDateTime createdAt;

    @Relationship(type = "POSTED", direction = Relationship.Direction.INCOMING)
    private User author;
}