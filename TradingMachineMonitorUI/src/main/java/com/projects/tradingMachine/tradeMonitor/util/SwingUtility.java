package com.projects.tradingMachine.tradeMonitor.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class SwingUtility {

	public static void setTableSorter(final JTable table, final int sortColumn, final SortOrder sortOrder) {
		final TableRowSorter<TableModel> tableSorter = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(tableSorter);
        final List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(sortColumn, sortOrder));
        tableSorter.setSortKeys(sortKeys);
        tableSorter.sort();
	}
	
}
