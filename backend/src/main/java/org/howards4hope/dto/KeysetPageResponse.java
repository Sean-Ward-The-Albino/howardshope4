package org.howards4hope.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Keyset (Cursor-based) Pagination Response.
 * Efficient O(1) pagination that eliminates standard OFFSET degradation on large datasets.
 */
public class KeysetPageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> items;
    private String nextCursorDate;
    private Long nextCursorId;
    private boolean hasNext;
    private int pageSize;

    public KeysetPageResponse() {}

    public KeysetPageResponse(List<T> items, String nextCursorDate, Long nextCursorId, boolean hasNext, int pageSize) {
        this.items = items;
        this.nextCursorDate = nextCursorDate;
        this.nextCursorId = nextCursorId;
        this.hasNext = hasNext;
        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public String getNextCursorDate() {
        return nextCursorDate;
    }

    public void setNextCursorDate(String nextCursorDate) {
        this.nextCursorDate = nextCursorDate;
    }

    public Long getNextCursorId() {
        return nextCursorId;
    }

    public void setNextCursorId(Long nextCursorId) {
        this.nextCursorId = nextCursorId;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
