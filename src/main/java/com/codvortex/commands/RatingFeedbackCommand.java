package com.codvortex.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingFeedbackCommand {
    private int rating;
    private String feedback;

}
