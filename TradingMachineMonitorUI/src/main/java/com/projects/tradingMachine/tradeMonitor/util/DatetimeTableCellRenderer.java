package com.projects.tradingMachine.tradeMonitor.util;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public final class DatetimeTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	private final DateFormat dateFormat;
	
	public DatetimeTableCellRenderer(final SimpleDateFormat dateFormat) {
		super();
		this.dateFormat = dateFormat;
	}
	
	@Override
	public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected, final boolean hasFocus,
			final int row, final int column) {
		value = dateFormat.format(value);
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
