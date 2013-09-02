/*
 * ReportDateFormat.java
 */

package com.eidp.Generator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author David Guzman
 * 
 * ReportDateFormat
 * Copyright (C) 2008  David Guzman
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public enum ReportDateFormat {
    en_GB("dd.MM.yyyy", Locale.UK),
    de_DE("dd.MM.yyyy", Locale.GERMANY),
    en_US("MM/dd/yyyy", Locale.US),
    fr_FR("dd.MM.yyyy", Locale.FRANCE)
    ;
    
    private final String format;
    private final Locale local;
    private final DateFormat isodf = new SimpleDateFormat("yyyy-MM-dd");
    
    ReportDateFormat(String format, Locale locale) {
        this.format = format;
        this.local = locale;
    }
    
    public String formatDate(String isoDate) throws ParseException {
        Date date = isodf.parse(isoDate);
        DateFormat outDf = new SimpleDateFormat(this.format);
        return outDf.format(date);
    }
    
    public String formatShortDate(String isoDate) throws ParseException {
        Date date = isodf.parse(isoDate);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, this.local);
        return df.format(date);
    }
    
    public String formatMediumDate(String isoDate) throws ParseException {
        Date date = isodf.parse(isoDate);
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, this.local);
        return df.format(date);
    }
    
    public String formatLongDate(String isoDate) throws ParseException {
        Date date = isodf.parse(isoDate);
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, this.local);
        return df.format(date);
    }

}
