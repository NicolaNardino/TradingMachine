package com.projects.tradingMachine.tradeMonitor.util;

import java.awt.Component;
import java.util.List;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.projects.tradingMachine.utility.marketData.MarketData;

public final class TooltipCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	private final List<MarketData> marketDataItems;
	
	public TooltipCellRenderer(final List<MarketData> marketDataItems) {
		this.marketDataItems = marketDataItems;
	}
	
	@Override
    public Component getTableCellRendererComponent(
                        final JTable table, final Object value,
                        final boolean isSelected, final boolean hasFocus,
                        final int row, final int column) {
        final JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        final Optional<MarketData> marketDataItem = marketDataItems.stream().filter((m) -> m.getID().equals(value)).findFirst();
        if (marketDataItem.isPresent())
        	label.setToolTipText(marketDataItem.toString());
        return label;
    }
}
