package com.WalkiePaw.presentation.domain.review.request;

import com.WalkiePaw.domain.board.entity.BoardCategory;
import com.WalkiePaw.domain.chatroom.entity.Chatroom;
import com.WalkiePaw.domain.member.entity.Member;
import com.WalkiePaw.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewSaveRequest {
    private final int point;
    private final String content;
    private final Long chatroomId;
    private final Long reviewerId;
    private final BoardCategory category;

    public static Review toEntity(final ReviewSaveRequest request, Chatroom chatroom, Member reviewee, Member reviewer) {
        return Review.builder()
            .point(request.point)
            .chatroom(chatroom)
            .reviewee(reviewee)
            .reviewer(reviewer)
            .content(request.content)
            .category(request.category)
            .build();
    }
}
