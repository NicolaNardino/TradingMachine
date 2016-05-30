package com.projects.tradingMachine.tradeMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.projects.tradingMachine.tradeMonitor.util.DatetimeTableCellRenderer;
import com.projects.tradingMachine.tradeMonitor.util.MarketDataSummary;
import com.projects.tradingMachine.tradeMonitor.util.PanelCleanUp;
import com.projects.tradingMachine.tradeMonitor.util.SwingUtility;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.marketData.MarketData;

/**
 * Market data panel with a table as main content.
 * */
public final class MarketDataPanel extends JPanel implements PanelCleanUp {
	private static final long serialVersionUID = 1L;
	private final List<MarketData> marketDataItems;
	private final List<MarketDataSummary> marketDataSummaryItems = new ArrayList<>();
	private final JTable marketDataTable;
	private final JTable marketDataSummaryTable;
	private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);

	public MarketDataPanel(final List<MarketData> marketDataItems) throws FileNotFoundException, IOException, JMSException {
		super(new BorderLayout(10, 20)); 
		this.marketDataItems = marketDataItems;
		buildMarketDataSummary();
		marketDataTable = buildMarketDataTable(false);
		marketDataSummaryTable = buildMarketDataTable(true);
		final JScrollPane marketDataSummaryScrollPanel = new JScrollPane(marketDataSummaryTable);
		marketDataSummaryScrollPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Market Data Summary"));
		add(marketDataSummaryScrollPanel, BorderLayout.NORTH);
		final JScrollPane marketDataScrollPanel = new JScrollPane(marketDataTable);
		marketDataScrollPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Market Data"));
		add(marketDataScrollPanel, BorderLayout.CENTER);
		es.scheduleWithFixedDelay(() -> {
			buildMarketDataSummary();
			((AbstractTableModel)marketDataSummaryTable.getModel()).fireTableDataChanged();
        }, 1, 1, TimeUnit.SECONDS); 
	}
	
	private void buildMarketDataSummary() {
		marketDataSummaryItems.clear();
		final ArrayList<MarketData> marketDataItemsCopy= new ArrayList<>(marketDataItems);
		final Map<String, Long> itemsNumber = marketDataItemsCopy.parallelStream().
				collect(Collectors.groupingBy(MarketData::getSymbol, Collectors.counting()));
		final Map<String, Double> avgBid = marketDataItemsCopy.parallelStream().
				collect(Collectors.groupingBy(MarketData::getSymbol, Collectors.averagingDouble(MarketData::getBid)));
		final Map<String, Double> avgAsk = marketDataItemsCopy.parallelStream().
				collect(Collectors.groupingBy(MarketData::getSymbol, Collectors.averagingDouble(MarketData::getAsk)));
		final Map<String, Double> avgBidSize = marketDataItemsCopy.parallelStream().
				collect(Collectors.groupingBy(MarketData::getSymbol, Collectors.averagingDouble(MarketData::getBidSize)));
		final Map<String, Double> avgAskSize = marketDataItemsCopy.parallelStream().
				collect(Collectors.groupingBy(MarketData::getSymbol, Collectors.averagingDouble(MarketData::getAskSize)));
		for(final Entry<String, Long> entry : itemsNumber.entrySet()) 
			marketDataSummaryItems.add(new MarketDataSummary(entry.getKey(), avgBid.get(entry.getKey()), avgAsk.get(entry.getKey()), avgBidSize.get(entry.getKey()), avgAskSize.get(entry.getKey()), entry.getValue()));
	}

	private JTable buildMarketDataTable(boolean isSummaryTable) throws FileNotFoundException, IOException, JMSException {
		final JTable marketDataTable = new JTable(isSummaryTable ? new MarketDataSummaryTableModel(marketDataSummaryItems) : new MarketDataTableModel(marketDataItems)) {
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
		if (isSummaryTable)
			SwingUtility.setTableSorter(marketDataTable, 0, SortOrder.ASCENDING);
		else {//sort on quote time --> column 6.
			marketDataTable.getColumnModel().getColumn(6).setCellRenderer(new DatetimeTableCellRenderer(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")));
			SwingUtility.setTableSorter(marketDataTable, 6, SortOrder.DESCENDING);
		}
		marketDataTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		marketDataTable.setFillsViewportHeight(true);
		return marketDataTable;
	}

	public JTable getMarketDataTable() {
		return marketDataTable;
	}

	public JTable getMarketDataSummaryTable() {
		return marketDataSummaryTable;
	}
	
	@Override
	public void cleanUp() throws Exception {
		Utility.shutdownExecutorService(es, 5, TimeUnit.SECONDS);
	}
}
