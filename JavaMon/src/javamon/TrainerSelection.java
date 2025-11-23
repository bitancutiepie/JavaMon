package javamon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class TrainerSelection extends JFrame {

    public TrainerSelection() {
        setTitle("Trainer Selection");
        setSize(1280, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);


        JPanel bgPanel = new JPanel() {
            private Image bg = new ImageIcon(
                    getClass().getResource("/javamon/assets/trainerBG.png")
            ).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null);
        setContentPane(bgPanel);


        // BORDER IMAGE (TOP LAYER)

        ImageIcon borderIcon = new ImageIcon(
                getClass().getResource("/javamon/assets/UPPERBORDERRR.png")
        );
        JLabel borderLabel = new JLabel(borderIcon);
        borderLabel.setBounds(0, -30, 1280, 200);
        bgPanel.add(borderLabel);


        // SELECTED TRAINER CLASS LABEL

        ImageIcon selectTrainerIcon = new ImageIcon(
                getClass().getResource("/javamon/assets/SELECTED TRAINER CLASS BUTT.png")
        );

        JLabel selectTrainerLabel = new JLabel(selectTrainerIcon);
        selectTrainerLabel.setBounds(1000, 155, 229, 94);
        bgPanel.add(selectTrainerLabel);
        bgPanel.setComponentZOrder(selectTrainerLabel, 1);


        // RECTANGLE (preview background)

        ImageIcon rectIcon;
        URL rectUrl = getClass().getResource("/javamon/assets/RECTANGLE.png");
        if (rectUrl != null) {
            rectIcon = new ImageIcon(rectUrl);
        } else {
            // fallback to uploaded file path (user-provided)
            rectIcon = new ImageIcon("/mnt/data/c7111513-60aa-42d3-93ba-c29139b50c70.png");
        }

        JLabel rectangle = new JLabel(rectIcon);
        int rectW = rectIcon.getIconWidth() > 0 ? rectIcon.getIconWidth() : 198;
        int rectH = rectIcon.getIconHeight() > 0 ? rectIcon.getIconHeight() : 371;
        rectangle.setBounds(1015, 253, rectW, rectH);
        rectangle.setLayout(null);
        bgPanel.add(rectangle);
        bgPanel.setComponentZOrder(rectangle, 1);

        // ============================
        // FLOATING PREVIEW (shows same-size card over the rectangle)
        // ============================
        // Start invisible; we'll set icon & position on selection
        JLabel floatingPreview = new JLabel();
        floatingPreview.setVisible(false);
        // Add to bgPanel (so it can overlap rectangle); we'll position it later when a card is clicked
        bgPanel.add(floatingPreview);
        // Ensure floatingPreview is above rectangle and other UI items
        bgPanel.setComponentZOrder(floatingPreview, 0); // we'll bring it to front after adding other things
        // after all adds we'll reorder appropriately below


        // BACK BUTTON

        ImageIcon backIcon = new ImageIcon(
                getClass().getResource("/javamon/assets/BACK BUTTON.png")
        );

        JButton backButton = new JButton(backIcon);
        backButton.setBounds(20, 630, 204, 84);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> {
            System.out.println("Back button clicked! Returning to main menu...");
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
            this.dispose();
        });

        bgPanel.add(backButton);
        bgPanel.setComponentZOrder(backButton, 1);

        // ============================
        // PROCEED BUTTON
        // ============================
        ImageIcon proceedIcon = new ImageIcon(
                getClass().getResource("/javamon/assets/PROCEED BUTTON.png")
        );
        JButton proceedButton = new JButton(proceedIcon);
        proceedButton.setBounds(1016, 630,
                proceedIcon.getIconWidth(),
                proceedIcon.getIconHeight());

        proceedButton.setBorderPainted(false);
        proceedButton.setContentAreaFilled(false);
        proceedButton.setFocusPainted(false);
        proceedButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        proceedButton.addActionListener(e -> {
            System.out.println("Proceed button clicked!");
            DraftSelection draft = new DraftSelection();
            draft.setVisible(true);
            this.dispose();
        });

        bgPanel.add(proceedButton);
        bgPanel.setComponentZOrder(proceedButton, 1);

        // Reorder floatingPreview to be above rectangle and other components
        // (setComponentZOrder: lower index = on top; adjust to ensure floatingPreview is top-most)
        bgPanel.setComponentZOrder(borderLabel, bgPanel.getComponentCount()-1); // send border to back
        bgPanel.setComponentZOrder(rectangle, bgPanel.getComponentCount()-2);
        bgPanel.setComponentZOrder(selectTrainerLabel, bgPanel.getComponentCount()-3);
        bgPanel.setComponentZOrder(proceedButton, bgPanel.getComponentCount()-4);
        bgPanel.setComponentZOrder(backButton, bgPanel.getComponentCount()-5);
        // finally bring floatingPreview to front
        bgPanel.setComponentZOrder(floatingPreview, 0);

        // ============================
        // TRAINER CARDS (click to show floating preview)
        // ============================
        String[] trainers = {"ELEMENTALIST", "STRATEGIST", "AGGRESSOR", "BEASTMASTER", "MYSTIC"};
        int[][] positions = {
                {26, 100, 267, 441},// elementalist
                {325, 95, 302, 456}, // strategist
                {637, 89, 298, 467},// aggressor
                {195, 320, 228, 460}, // beastmaster
                {502, 328, 270, 441} // mystic
        };

        for (int i = 0; i < trainers.length; i++) {
            String trainer = trainers[i];
            int x = positions[i][0];
            int y = positions[i][1];
            int w = positions[i][2];
            int h = positions[i][3];

            ImageIcon icon;
            URL iconUrl = getClass().getResource("/javamon/assets/" + trainer + ".png");
            if (iconUrl != null) {
                icon = new ImageIcon(iconUrl);
            } else {
                icon = new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
            }

            JButton trainerButton = new JButton(icon);
            trainerButton.setBounds(x, y, w, h);
            trainerButton.setBorderPainted(false);
            trainerButton.setContentAreaFilled(false);
            trainerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            final ImageIcon buttonIcon = icon; // capture for listener

            trainerButton.addActionListener(e -> {
                System.out.println(trainer + " selected!");

                // Use the same icon as the button (so it "looks like" the card on the left)
                ImageIcon previewIcon = buttonIcon;

                // compute position so floating preview is centered over the rectangle
                int fw = previewIcon.getIconWidth();
                int fh = previewIcon.getIconHeight();

                // if icon has invalid dimensions, fallback to a reasonable size
                if (fw <= 0) fw = w;
                if (fh <= 0) fh = h;

                int rectX = rectangle.getX();
                int rectY = rectangle.getY();
                int rectWidth = rectangle.getWidth();
                int rectHeight = rectangle.getHeight();

                int fx = rectX + (rectWidth - fw) / 2;
                int fy = rectY + (rectHeight - fh) / 2;

                // set icon and bounds on the floating preview label
                floatingPreview.setIcon(previewIcon);
                floatingPreview.setBounds(fx, fy, fw, fh);
                floatingPreview.setVisible(true);

                // bring floatingPreview to front so it overlaps everything
                bgPanel.setComponentZOrder(floatingPreview, 0);
                bgPanel.revalidate();
                bgPanel.repaint();
            });

            bgPanel.add(trainerButton);
            bgPanel.setComponentZOrder(trainerButton, 1);
        }

        setVisible(true);
    }
}
