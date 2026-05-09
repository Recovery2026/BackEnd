package com.example.recovery.controller.memoirs;

import com.example.recovery.request.*;
import com.example.recovery.response.MemoirCalenderResponse;
import com.example.recovery.response.MemoirResponse;
import com.example.recovery.response.MemoirSimpleResponse;
import com.example.recovery.service.memoirs.MemoirService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/memoirs")
@RequiredArgsConstructor
public class MemoirController {

    private final MemoirService memoirService;

    @PostMapping(params = "!date")
    public MemoirSimpleResponse viewMemoirsByList(@Validated @RequestBody MemoirBodyRequest request, @Validated @ModelAttribute SimplePageRequest simplePageRequest) {
        return memoirService.getMemoirList(request, simplePageRequest);
    }

    @PostMapping(params = "date")
    public MemoirCalenderResponse viewMemoirByCalender(@Validated @RequestBody MemoirBodyRequest request, @Validated @ModelAttribute MemoirCalenderRequest calenderRequest) {
        return memoirService.getMemoirCalender(request, calenderRequest);
    }

    @PostMapping("/{memoirId:\\d+}")
    public MemoirResponse viewMemoir(@Validated @RequestBody MemoirBodyRequest request, @PathVariable Long memoirId) {
        return memoirService.getMemoir(request, memoirId);
    }

    @PutMapping("/{memoirId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMemoir(@Validated @RequestBody MemoirUpdateRequest request, @PathVariable Long memoirId) {
        memoirService.updateMemoir(request, memoirId);
    }

    @PostMapping("/memoir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void writeMemoir(@Validated @RequestBody MemoirWriteRequest request) {
        memoirService.writeMemoir(request);
    }

    @PutMapping("/{memoirId:\\d+}/improvement")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateImprovement(@Validated @RequestBody MemoirUpdateRequest request, @PathVariable Long memoirId) {
        memoirService.updateImprovement(request, memoirId);
    }
}
