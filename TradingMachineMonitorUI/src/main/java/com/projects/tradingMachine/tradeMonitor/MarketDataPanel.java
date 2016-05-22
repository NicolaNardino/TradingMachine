package com.projects.tradingMachine.tradeMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.TableCellRenderer;

import com.projects.tradingMachine.tradeMonitor.util.DatetimeTableCellRenderer;
import com.projects.tradingMachine.tradeMonitor.util.SwingUtility;
import com.projects.tradingMachine.utility.marketData.MarketData;

/**
 * Market data panel with a table as main content.
 * */
public final class MarketDataPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final List<MarketData> marketDataItems;
	private final JTable marketDataTable;

	public MarketDataPanel(List<MarketData> marketDataItems) throws FileNotFoundException, IOException, JMSException {
		super(new BorderLayout(10, 20)); 
		this.marketDataItems = marketDataItems;
		marketDataTable = buildMarketDataTable();
		add(new JScrollPane(marketDataTable), BorderLayout.CENTER);
	}

	private JTable buildMarketDataTable() throws FileNotFoundException, IOException, JMSException {
		final JTable marketDataTable = new JTable(new MarketDataTableModel(marketDataItems)) {
			private static final long serialVersionUID = 1L;
			public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column)
			{
				final Component component = super.prepareRenderer(renderer, row, column);
				if (row % 2 == 0) {
					component.setBackground(Color.WHITE);
					component.setForeground(Color.BLACK);
				}
				else {
					component.setBackground(Color.GREEN);
					component.setForeground(Color.RED);
				}
				return component;
			}
		};
		marketDataTable.getColumnModel().getColumn(6).setCellRenderer(new DatetimeTableCellRenderer(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")));
		//sort on quote time.
		SwingUtility.setTableSorter(marketDataTable, 6, SortOrder.DESCENDING);
		marketDataTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		marketDataTable.setFillsViewportHeight(true);
		return marketDataTable;
	}

	public JTable getMarketDataTable() {
		return marketDataTable;
	}

}
