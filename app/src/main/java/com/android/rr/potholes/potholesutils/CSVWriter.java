/*
package com.android.rr.potholes.potholesutils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class CSVWriter {
    private PrintWriter mPrintWriter;
    private char mSeparator;
    private char mEscapechar;
    private String mLineEnd;
    private char mQuotechar;

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char NO_QUOTE_CHARACTER = '\u0000';
    private static final char NO_ESCAPE_CHARACTER = '\u0000';
    private static final String DEFAULT_LINE_END = "\n";
    private static final char DEFAULT_QUOTE_CHARACTER = '"';
    private static final char DEFAULT_ESCAPE_CHARACTER = '"';

    public CSVWriter(Writer writer) {
        this(writer, DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER,
                DEFAULT_ESCAPE_CHARACTER, DEFAULT_LINE_END);
    }

    private CSVWriter(Writer writer, char mSeparator, char mQuotechar,
                      char mEscapechar, String mLineEnd) {
        this.mPrintWriter = new PrintWriter(writer);
        this.mSeparator = mSeparator;
        this.mQuotechar = mQuotechar;
        this.mEscapechar = mEscapechar;
        this.mLineEnd = mLineEnd;
    }

    public void writeNext(String[] nextLine) {
        if (nextLine == null)
            return;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nextLine.length; i++) {

            if (i != 0) {
                sb.append(mSeparator);
            }

            String nextElement = nextLine[i];
            if (nextElement == null)
                continue;
            if (mQuotechar != NO_QUOTE_CHARACTER)
                sb.append(mQuotechar);
            for (int j = 0; j < nextElement.length(); j++) {
                char nextChar = nextElement.charAt(j);
                if (mEscapechar != NO_ESCAPE_CHARACTER && nextChar == mQuotechar) {
                    sb.append(mEscapechar).append(nextChar);
                } else if (mEscapechar != NO_ESCAPE_CHARACTER && nextChar == mEscapechar) {
                    sb.append(mEscapechar).append(nextChar);
                } else {
                    sb.append(nextChar);
                }
            }
            if (mQuotechar != NO_QUOTE_CHARACTER)
                sb.append(mQuotechar);
        }

        sb.append(mLineEnd);
        mPrintWriter.write(sb.toString());
    }

    public void close() {
        mPrintWriter.flush();
        mPrintWriter.close();
    }

    public void flush() {
        mPrintWriter.flush();
    }

}*/
