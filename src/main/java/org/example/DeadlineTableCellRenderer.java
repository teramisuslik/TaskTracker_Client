package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class DeadlineTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Применяем цвет только к колонке дедлайна (колонка 3)
        if (column == 3 && value != null) {
            String deadline = value.toString();
            if (!deadline.equals("нет дедлайна")) {
                c.setForeground(DeadlineUtils.getDeadlineColor(deadline));
                ((JLabel) c).setFont(new Font("Arial", Font.BOLD, 12));
            } else {
                c.setForeground(Color.GRAY);
            }
        } else {
            c.setForeground(Color.BLACK);
        }

        if (isSelected) {
            c.setBackground(new Color(52, 152, 219));
            c.setForeground(Color.WHITE);
        } else {
            c.setBackground(row % 2 == 0 ?
                    new Color(248, 249, 250) : Color.WHITE);
        }

        return c;
    }
}
