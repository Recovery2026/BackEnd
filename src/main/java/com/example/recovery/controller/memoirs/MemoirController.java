package com.example.recovery.controller.memoirs;

import com.example.recovery.request.MemoirCalenderRequest;
import com.example.recovery.request.MemoirUpdateRequest;
import com.example.recovery.request.MemoirWriteRequest;
import com.example.recovery.request.SimplePageRequest;
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
    public MemoirSimpleResponse viewMemoirsByList(@Validated @ModelAttribute SimplePageRequest simplePageRequest) {
        return memoirService.getMemoirList(simplePageRequest);
    }

    @PostMapping(params = "date")
    public MemoirCalenderResponse viewMemoirByCalender(@Validated @ModelAttribute MemoirCalenderRequest calenderRequest) {
        return memoirService.getMemoirCalender(calenderRequest);
    }

    @PostMapping("/{memoirId:\\d+}")
    public MemoirResponse viewMemoir(@PathVariable Long memoirId) {
        return memoirService.getMemoir(memoirId);
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
