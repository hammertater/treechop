package ht.treechop.client.gui.screen;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import ht.treechop.client.gui.element.*;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.StickyWidget;
import ht.treechop.client.gui.widget.ToggleWidget;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

public class ClientSettingsScreen extends Screen {

    private static final int ROW_SEPARATION = 1;
    private static final boolean IS_PAUSE_SCREEN = true;
    private static final int SPACE_ABOVE_AND_BELOW_LIST = 10;
    private static final int MIN_HEIGHT = (GUIUtil.BUTTON_HEIGHT + ROW_SEPARATION) * 5 - ROW_SEPARATION;
    private static final int TEXT_HEIGHT = 9;

    protected RowsGui optionsList;
    private Button doneButton;
    private int optionsPage = 0;
    private boolean needToRebuild = false;

    public ClientSettingsScreen() {
        super(Component.translatable("treechop.gui.settings.title", TreeChop.MOD_NAME));
    }

    @Override
    protected void init() {
        super.init();
        rebuild();
    }

    private void rebuild() {
        List<NestedGui> optionRows = optionsPage == 0 ? makePageOne() : makePageTwo();

        addBufferRows(optionRows);

        optionRows.add(
                new ButtonGui(
                        optionsPage == 0 ? Sprite.PAGE_ONE : Sprite.PAGE_TWO,
                        optionsPage == 0 ? Sprite.HIGHLIGHTED_PAGE_ONE : Sprite.HIGHLIGHTED_PAGE_TWO,
                        () -> {
                            needToRebuild = true;
                            optionsPage = optionsPage == 0 ? 1 : 0;
                        }
                )
        );

        this.optionsList = addRenderableWidget(new RowsGui(
                ROW_SEPARATION,
                optionRows
        ));
        placeOptionsList();

        final int doneButtonWidth = 200;
        doneButton = addRenderableWidget(new Button.Builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds((width - doneButtonWidth) / 2,
                                getDoneButtonTop(),
                                doneButtonWidth,
                                GUIUtil.BUTTON_HEIGHT)
                        .build()
        );

        int titleTop = optionsList.getY() - SPACE_ABOVE_AND_BELOW_LIST - GUIUtil.TEXT_LINE_HEIGHT;
        addRenderableWidget(new StringWidget(0, titleTop, this.width, TEXT_HEIGHT, this.title, this.font).alignCenter());
    }

    private void placeOptionsList() {
        int top = 32;
        int bottom = height - 32;
        int middle = (top + bottom) / 2;
        int listTop = middle - optionsList.getHeight() / 2;
        int listBottom = middle + optionsList.getHeight() / 2;
        optionsList.setBox(0, listTop, width, listBottom - listTop);
    }

    private void addBufferRows(List<NestedGui> rows) {
        int missingHeight = MIN_HEIGHT - RowsGui.getHeightForRows(rows, ROW_SEPARATION) - ROW_SEPARATION * 2;
        if (missingHeight > 0) {
            rows.add(0, new EmptyGui(0, Math.floorDiv(missingHeight, 2)));
            rows.add(new EmptyGui(0, (int) Math.ceil((float)missingHeight / 2)));
        }
    }

    private List<NestedGui> makePageOne() {
        LinkedList<NestedGui> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.chopping"),
                        makeToggleSettingRow(SettingsField.CHOPPING, "treechop.gui.settings.tooltip.chopping")
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.sneaking_inverts_chopping"),
                        new ToggleGui(
                                () -> Client.getChopSettings().setSneakBehavior(getNextSneakBehavior()),
                                () -> ToggleWidget.State.of(
                                        Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                                        isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, getNextSneakBehavior())
                                ),
                                this::getSneakCycleTooltip
                        )
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.only_chop_trees_with_leaves"),
                        makeToggleSettingRow(SettingsField.TREES_MUST_HAVE_LEAVES, "treechop.gui.settings.tooltip.only_chop_trees_with_leaves")
                )
        );

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            optionRows.add(
                    new LabeledGui(font,
                            Component.translatable("treechop.gui.settings.label.chop_in_creative_mode"),
                            makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE, "treechop.gui.settings.tooltip.chop_in_creative_mode")
                    )
            );
        }

        return optionRows;
    }

    private LinkedList<NestedGui> makePageTwo() {
        LinkedList<NestedGui> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.chop_in_creative_mode"),
                        makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE, "treechop.gui.settings.tooltip.chop_in_creative_mode")
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.chopping_indicator"),
                        new ToggleGui(
                                () -> Client.setChoppingIndicatorVisibility(!Client.isChoppingIndicatorEnabled()),
                                () -> ToggleWidget.State.of(Client.isChoppingIndicatorEnabled(), true),
                                () -> Component.translatable("treechop.gui.settings.tooltip.chopping_indicator"
                                        + (Client.isChoppingIndicatorEnabled() ? ".on" : ".off"))
                        )
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.feedback_messages"),
                        new ToggleGui(
                                () -> ConfigHandler.CLIENT.showFeedbackMessages.set(!ConfigHandler.CLIENT.showFeedbackMessages.get()),
                                () -> ToggleWidget.State.of(ConfigHandler.CLIENT.showFeedbackMessages.get(), true),
                                () -> Component.translatable("treechop.gui.settings.tooltip.feedback_messages"
                                        + (ConfigHandler.CLIENT.showFeedbackMessages.get() ? ".on" : ".off"))
                        )
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        Component.translatable("treechop.gui.settings.label.tooltips"),
                        new ToggleGui(
                                () -> ConfigHandler.CLIENT.showTooltips.set(!ConfigHandler.CLIENT.showTooltips.get()),
                                () -> ToggleWidget.State.of(
                                        ConfigHandler.CLIENT.showTooltips.get(),
                                        true
                                ),
                                () -> Component.translatable("treechop.gui.settings.tooltip.tooltips")
                        )
                )
        );

        return optionRows;
    }

    private Component getSneakCycleTooltip() {
        SettingsField field;
        switch (Client.getChopSettings().getSneakBehavior()) {
            case INVERT_CHOPPING -> field = SettingsField.CHOPPING;
            default -> {
                return Component.translatable("treechop.gui.settings.tooltip.sneaking_does_nothing");
            }
        }

        String enablesOrDisablesLangKey = Client.getChopSettings().get(field, Boolean.class)
                ? "treechop.gui.settings.tooltip.sneaking_enables_x"
                : "treechop.gui.settings.tooltip.sneaking_disables_x";

        return Component.translatable(enablesOrDisablesLangKey, field.getFancyName());
    }

    private SneakBehavior getNextSneakBehavior() {
        return Client.getChopSettings().getSneakBehavior() == SneakBehavior.NONE ? SneakBehavior.INVERT_CHOPPING : SneakBehavior.NONE;
    }

    private ToggleGui makeToggleSettingRow(SettingsField field, String tooltipLangKey) {
        return new ToggleGui(
                () -> Client.getChopSettings().set(field, !Client.getChopSettings().get(field, Boolean.class)),
                () -> ToggleWidget.State.of(
                        Client.getChopSettings().get(field, Boolean.class),
                        Client.getServerPermissions().isPermitted(new Setting(field, !Client.getChopSettings().get(field, Boolean.class)))
                ),
                () -> Component.translatable(tooltipLangKey +
                        (Client.getChopSettings().get(field, Boolean.class) ? ".on" : ".off"))
        );
    }

    private boolean isSettingPermitted(SettingsField field, Object value) {
        return Client.getServerPermissions().isPermitted(new Setting(field, value));
    }

    private StickyWidget.State makeStickyWidgetState(SettingsField field, Object value) {
        return StickyWidget.State.of(
                Client.getChopSettings().get(field) == value,
                Client.getServerPermissions().isPermitted(new Setting(field, value))
        );
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        if (needToRebuild) {
            clearWidgets();
            rebuild();
            needToRebuild = false;
        }

        doneButton.setY(getDoneButtonTop());

        super.render(gui, mouseX, mouseY, partialTicks);
        // TODO: check out ClientSettingsScreen.func_243293_a for draw reordering; might be important for tooltips

        if (ConfigHandler.CLIENT.showTooltips.get()) {
            GUIUtil.renderTooltip(gui);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return IS_PAUSE_SCREEN;
    }

    protected int getDoneButtonTop() {
        return optionsList.getBottom() + SPACE_ABOVE_AND_BELOW_LIST;
    }
}
