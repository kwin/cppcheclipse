package com.googlecode.cppcheclipse.core.utils;

/**
 * Search the data byte array for the first occurrence
 * of the byte array pattern (Knuth-Morris-Pratt Pattern)
 */
public class PatternSearch {
	private static final byte[] LINEBREAK_PATTERN = { '\n'};
	
	private final int[] failure;
	private final byte[] pattern;
	
	public static PatternSearch getLinebreakPatternSearch() {
		return new PatternSearch(LINEBREAK_PATTERN);
	}
	
	public PatternSearch(byte[] pattern) {
		this.pattern = pattern;
		failure = computeFailure(pattern);
	}
	
	public int indexAfter(byte[] data) {
		return indexAfter(data, 0);
	}
	
	public int indexAfter(byte[] data, int startPosition) {
		int result = indexOf(data, startPosition);
		if (result == -1) {
			return result;
		}
		return result + pattern.length;
	}
	
	public int indexOf(byte[] data) {
		return indexOf(data, 0);
	}
	
	/**
     * Search the data byte array for the first occurrence
     * of the byte array pattern 
     */
    public int indexOf(byte[] data, int startPosition) {
        int j = 0;

        for (int i = startPosition; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j>0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
}
