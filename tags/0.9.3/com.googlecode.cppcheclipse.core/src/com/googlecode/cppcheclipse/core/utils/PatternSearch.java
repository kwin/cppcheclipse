package com.googlecode.cppcheclipse.core.utils;

public class PatternSearch {
	private static final byte[] LINEBREAK_PATTERN = { '\n'};
	
	public static int indexOfLinebreak(byte[] data) {
		return indexOf(data, LINEBREAK_PATTERN);
	}
	
	public static int indexAfterLinebreak(byte[] data) {
		return indexAfter(data, LINEBREAK_PATTERN);
	}
	
	public static int indexAfter(byte[] data, byte[] pattern) {
		int result = indexOf(data, pattern);
		if (result == -1) {
			return result;
		}
		return result + pattern.length;
	}
	
	/**
     * Search the data byte array for the first occurrence
     * of the byte array pattern (Knuth-Morris-Pratt Pattern)
     */
    public static int indexOf(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;

        for (int i = 0; i < data.length; i++) {
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
