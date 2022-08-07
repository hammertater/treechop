package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.TreeChopMod;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

public class ClientSettingsScreen extends Screen {

    private static final int ROW_SEPARATION = 1;
    private static final int INSET_SIZE = 20;
    private static final boolean IS_PAUSE_SCREEN = true;
    private static final int SPACE_ABOVE_AND_BELOW_LIST = 10;
    private static final int MIN_HEIGHT = (GUIUtil.BUTTON_HEIGHT + ROW_SEPARATION) * 5 - ROW_SEPARATION;

    protected RowsGui optionsList;
    private Button doneButton;
    private int optionsPage = 0;
    private boolean needToRebuild = false;

    public ClientSettingsScreen() {
        super(Component.translatable("treechop.gui.settings.title", TreeChopMod.MOD_NAME));
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

        final int doneButtonWidth = 200;
        doneButton = addRenderableWidget(new Button(
                (width - doneButtonWidth) / 2,
                getDoneButtonTop(),
                doneButtonWidth,
                GUIUtil.BUTTON_HEIGHT,
                Component.translatable("gui.done"),
                button -> onClose()
        ));
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

        if (ConfigHandler.CLIENT.showFellingOptions.get()) {
            optionRows.add(
                    new LabeledGui(font,
                            Component.translatable("treechop.gui.settings.label.felling"),
                            makeToggleSettingRow(SettingsField.FELLING, "treechop.gui.settings.tooltip.felling")
                    )
            );

            optionRows.add(
                    new LabeledGui(font,
                            Component.translatable("treechop.gui.settings.label.sneaking_inverts"),
                            new ExclusiveButtonsGui.Builder()
                                    .add(
                                            Component.translatable("treechop.gui.settings.button.chopping"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_CHOPPING),
                                            () -> StickyWidget.State.of(
                                                    Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                                                    isSettingPermitted(SettingsField.CHOPPING, !Client.getChopSettings().getChoppingEnabled())
                                                            && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_CHOPPING)
                                            )
                                    )
                                    .add(
                                            Component.translatable("treechop.gui.settings.button.felling"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_FELLING),
                                            () -> StickyWidget.State.of(
                                                    Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_FELLING,
                                                    isSettingPermitted(SettingsField.FELLING, !Client.getChopSettings().getFellingEnabled())
                                                            && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_FELLING)
                                            )
                                    )
                                    .add(
                                            Component.translatable("treechop.gui.settings.button.nothing"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.NONE),
                                            () -> makeStickyWidgetState(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.NONE)
                                    )
                                    .build(this::getSneakCycleTooltip)
                    )
            );
        }
        else {
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
        }

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
                        Component.translatable("treechop.gui.settings.label.felling_options"),
                        new ToggleGui(
                                () -> ConfigHandler.CLIENT.showFellingOptions.set(!ConfigHandler.CLIENT.showFellingOptions.get()),
                                () -> ToggleWidget.State.of(
                                        ConfigHandler.CLIENT.showFellingOptions.get(),
                                        Client.getServerPermissions().isPermitted(new Setting(SettingsField.FELLING, false))
                                ),
                                () -> Component.translatable("treechop.gui.settings.tooltip.felling_options"
                                        + (ConfigHandler.CLIENT.showFellingOptions.get() ? ".on" : ".off"))
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
            case INVERT_FELLING -> field = SettingsField.FELLING;
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (needToRebuild) {
            clearWidgets();
            rebuild();
            needToRebuild = false;
        }

        renderBackground(poseStack);

        doneButton.y = getDoneButtonTop();

        int listTop = getListTop();
        int listBottom = getListBottom();
        optionsList.setBox(0, listTop, width, listBottom - listTop);
        optionsList.render(poseStack, mouseX, mouseY, partialTicks);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, getTitleTop(), 16777215);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        // TODO: check out ClientSettingsScreen.func_243293_a for draw reordering; might be important for tooltips

        if (ConfigHandler.CLIENT.showTooltips.get()) {
            GUIUtil.renderTooltip(poseStack);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);
        fill(poseStack, INSET_SIZE, INSET_SIZE, width - INSET_SIZE, height - INSET_SIZE, 0x00000080);
    }

    @Override
    public boolean isPauseScreen() {
        return IS_PAUSE_SCREEN;
    }

    protected int getTop() {
        return 32;
    }

    protected int getBottom() {
        return height - 32;
    }

    protected int getMiddleY() {
        return (getTop() + getBottom()) / 2;
    }

    protected int getTitleTop() {
        return getListTop() - SPACE_ABOVE_AND_BELOW_LIST - GUIUtil.TEXT_LINE_HEIGHT;
    }

    protected int getListTop() {
        return getMiddleY() - getListHeight() / 2;
    }

    protected int getListHeight() {
        return optionsList.getHeight();
    }

    protected int getListBottom() {
        return getMiddleY() + getListHeight() / 2;
    }

    protected int getDoneButtonTop() {
        return getListBottom() + SPACE_ABOVE_AND_BELOW_LIST;
    }
}
