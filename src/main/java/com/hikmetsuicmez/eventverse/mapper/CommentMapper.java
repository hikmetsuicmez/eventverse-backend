package com.hikmetsuicmez.eventverse.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hikmetsuicmez.eventverse.dto.response.CommentResponse;
import com.hikmetsuicmez.eventverse.dto.response.ReplyResponse;
import com.hikmetsuicmez.eventverse.entity.Comment;
import com.hikmetsuicmez.eventverse.entity.Reply;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "replies", source = "replies")
    CommentResponse toCommentResponse(Comment comment);

    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "user.firstName", source = "user.firstName")
    @Mapping(target = "user.lastName", source = "user.lastName")
    @Mapping(target = "user.profilePicture", source = "user.profilePicture")
    @Mapping(target = "isEventOwnerReply", source = "eventOwnerReply")
    ReplyResponse toReplyResponse(Reply reply);

    List<CommentResponse> toCommentResponseList(List<Comment> comments);

}
