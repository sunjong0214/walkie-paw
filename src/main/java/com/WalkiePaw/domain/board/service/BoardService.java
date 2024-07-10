package com.WalkiePaw.domain.board.service;

import com.WalkiePaw.domain.board.entity.Board;
import com.WalkiePaw.domain.board.entity.BoardCategory;
import com.WalkiePaw.domain.board.entity.BoardPhoto;
import com.WalkiePaw.domain.board.repository.BoardPhotoRepository;
import com.WalkiePaw.domain.board.repository.BoardRepository;
import com.WalkiePaw.domain.member.Repository.MemberRepository;
import com.WalkiePaw.domain.member.entity.Member;
import com.WalkiePaw.global.exception.BadRequestException;
import com.WalkiePaw.presentation.domain.board.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.WalkiePaw.global.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final BoardPhotoRepository boardPhotoRepository;

    public List<BoardListResponse> findAllBoardAndMember(final BoardCategory category) {
        List<Board> findBoards = boardRepository.findAllNotDeleted(category);
        return findBoards.stream()
                .map(BoardListResponse::from)
                .toList();
    }

    @Transactional
    public Integer save(final BoardAddRequest request) {
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow();
        Board entity = BoardAddRequest.toEntity(request, member);
        Board board = boardRepository.save(entity);
        request.getPhotos().stream()
                .map(BoardPhoto::new)
                .map(boardPhotoRepository::save)
                .toList().forEach(p -> p.addPhoto(board));
        return board.getId();
    }


    public BoardGetResponse getBoard(Integer boardId) {
        Board board = boardRepository.getBoardDetail(boardId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_BOARD_ID));
        List<String> photoUrls = board.getPhotoUrls(board);
        return BoardGetResponse.from(board, photoUrls);
    }

    @Transactional
    public void updateBoard(final Integer boardId, final BoardUpdateRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_BOARD_ID));
        board.updateBoard(request.getTitle(), request.getContent(), request.getPrice(), request.getStartTime(),
                request.getEndTime(), request.getPriceType(), request.getLocation(), request.getDetailedLocation(), request.isPriceProposal());
        board.updatePhoto(request.getPhotos());
    }

    @Transactional
    public void updateBoardStatus(final BoardStatusUpdateRequest request) {
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_BOARD_ID));
        board.updateStatus(request.getStatus());
    }

    @Transactional
    public void deleteBoard(final Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BadRequestException(NOT_FOUND_BOARD_ID));
        board.delete();
    }

    public List<BoardListResponse> findBySearchCond(final String title, final String content, final BoardCategory category) {
        List<Board> bySearchCond = boardRepository.findBySearchCond(title, content, category);
        return bySearchCond.stream()
                .map(BoardListResponse::from)
                .toList();
    }

    public List<BoardMypageListResponse> findMyBoardsBy(final Integer memberId, final BoardCategory category) {
        return boardRepository.findMyBoardsBy(memberId, category);
    }
//
//    @Transactional
//    public Integer uploadPhoto(final Integer boardId, final String uploadURL, final String oriName) {
//        Board board = boardRepository.findById(boardId)
//                .orElseThrow(() -> new BadRequestException(NOT_FOUND_BOARD_ID));
//        BoardPhoto boardPhoto = new BoardPhoto(oriName, uploadURL, board);
//        boardPhoto.addPhoto(board);
//        return boardPhotoRepository.save(boardPhoto).getId();
//    }
}
