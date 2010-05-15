package com.googlecode.cppcheclipse.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class LineFilterOutputStream extends FilterOutputStream {

	private final ByteArrayOutputStream lineBuffer;
	private final PatternSearch linebreakPatternSearch;
	private final String charset;
	private final Collection<Pattern> blacklistPatterns;

	public LineFilterOutputStream(OutputStream out, String charset) {
		super(out);
		lineBuffer = new ByteArrayOutputStream();
		linebreakPatternSearch = PatternSearch.getLinebreakPatternSearch();
		this.charset = charset;
		blacklistPatterns = new LinkedList<Pattern>();
	}
	
	public void addBlacklistPattern(String regex) {
		blacklistPatterns.add(Pattern.compile(regex));
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		lineBuffer.write(b, off, len);
		flushBuffer();
	}

	private boolean runFilters(byte[] buffer, int offset, int length) throws UnsupportedEncodingException {
		boolean skipLine = false;
		if (!blacklistPatterns.isEmpty()) {
			String line = new String(buffer, offset, length, charset);
			for (Pattern blacklistPattern : blacklistPatterns) {
				if (blacklistPattern.matcher(line).matches()) {
					skipLine = true;
					break;
				}
			}
		} 
		return skipLine;
	}
	
	private void flushLine(byte[] buffer) throws IOException {
		flushLine(buffer, 0, buffer.length);
	}
	
	private void flushLine(byte[] buffer, int offset, int length) throws IOException {
		if (!runFilters(buffer, offset, length)) {
			out.write(buffer, offset, length);
		}
	}
	
	/**
	 * Give out every complete line to underlying outputstream
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean flushBuffer() throws IOException {
		byte[] buffer = lineBuffer.toByteArray();
		int startPos = 0;
		int pos;
		boolean foundLineBreak = false;
		while ((pos = linebreakPatternSearch.indexAfter(buffer, startPos)) >= 0) {
			flushLine(buffer, startPos, pos - startPos);
			startPos = pos;
			foundLineBreak = true;
		}
		if (foundLineBreak) {
			lineBuffer.reset();
			lineBuffer.write(buffer, startPos, buffer.length - startPos);
		}

		return false;
	}

	@Override
	public void write(int b) throws IOException {
		lineBuffer.write(b);
		flushBuffer();
	}

	@Override
	public void close() throws IOException {
		flushLine(lineBuffer.toByteArray());
		super.close();
	}
}
