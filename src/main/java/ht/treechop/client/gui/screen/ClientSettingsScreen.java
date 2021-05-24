package ht.treechop.client.gui.screen;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.client.KeyBindings;
import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.element.ButtonGui;
import ht.treechop.client.gui.element.EmptyGui;
import ht.treechop.client.gui.element.ExclusiveButtonsGui;
import ht.treechop.client.gui.element.LabeledGui;
import ht.treechop.client.gui.element.NestedGui;
import ht.treechop.client.gui.element.RowsGui;
import ht.treechop.client.gui.element.TextButtonGui;
import ht.treechop.client.gui.element.ToggleGui;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.StickyWidget;
import ht.treechop.client.gui.widget.ToggleWidget;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ClientSettingsScreen extends Screen {

    private static final int ROW_SEPARATION = 1;
    private static final boolean IS_PAUSE_SCREEN = true;
    private static final int SPACE_ABOVE_AND_BELOW_LIST = 20;
    private static final int MIN_HEIGHT = (GUIUtil.BUTTON_HEIGHT + ROW_SEPARATION) * 4 - ROW_SEPARATION;

    protected RowsGui optionsList;
    private NestedGui doneButton;
    private int optionsPage = 0;
    private boolean needToRebuild = false;
    private final List<NestedGui> children = new LinkedList<>();

    @Override
    public void initGui() {
        super.initGui();
        rebuild();
    }

    private void rebuild() {
        children.clear();
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

        this.optionsList = (RowsGui) addListener(new RowsGui(
                ROW_SEPARATION,
                optionRows
        ));

        final int doneButtonWidth = 200;
        doneButton = addListener(new TextButtonGui(
                (width - doneButtonWidth) / 2,
                getDoneButtonTop(),
                doneButtonWidth,
                GUIUtil.BUTTON_HEIGHT,
                new TextComponentTranslation("gui.done"),
                ClientSettingsScreen::closeScreen
        ));
    }

    public static void closeScreen() {
        if (Minecraft.getMinecraft().currentScreen instanceof ClientSettingsScreen) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    private NestedGui addListener(NestedGui gui) {
        children.add(gui);
        return gui;
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
                new LabeledGui(fontRenderer,
                        new TextComponentTranslation("treechop.gui.settings.label.chopping"),
                        makeToggleSettingRow(SettingsField.CHOPPING)
                )
        );

        if (ConfigHandler.CLIENT.showFellingOptions.get()) {
            optionRows.add(
                    new LabeledGui(fontRenderer,
                            new TextComponentTranslation("treechop.gui.settings.label.felling"),
                            makeToggleSettingRow(SettingsField.FELLING)
                    )
            );

            optionRows.add(
                    new LabeledGui(fontRenderer,
                            new TextComponentTranslation("treechop.gui.settings.label.sneaking_inverts"),
                            new ExclusiveButtonsGui.Builder()
                                    .add(
                                            new TextComponentTranslation("treechop.gui.settings.button.chopping"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_CHOPPING),
                                            () -> StickyWidget.State.of(
                                                    Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                                                    isSettingPermitted(SettingsField.CHOPPING, !Client.getChopSettings().getChoppingEnabled())
                                                            && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_CHOPPING)
                                            )
                                    )
                                    .add(
                                            new TextComponentTranslation("treechop.gui.settings.button.felling"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_FELLING),
                                            () -> StickyWidget.State.of(
                                                    Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_FELLING,
                                                    isSettingPermitted(SettingsField.FELLING, !Client.getChopSettings().getFellingEnabled())
                                                            && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_FELLING)
                                            )
                                    )
                                    .add(
                                            new TextComponentTranslation("treechop.gui.settings.button.nothing"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.NONE),
                                            () -> makeStickyWidgetState(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.NONE)
                                    )
                                    .build()
                    )
            );
        }
        else {
            optionRows.add(
                    new LabeledGui(fontRenderer,
                            new TextComponentTranslation("treechop.gui.settings.label.sneaking_inverts_chopping"),
                            new ToggleGui(
                                    () -> Client.getChopSettings().setSneakBehavior(getNextSneakBehavior()),
                                    () -> ToggleWidget.State.of(
                                            Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                                            isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, getNextSneakBehavior())
                                    )
                            )
                    )
            );
        }

        optionRows.add(
                new LabeledGui(fontRenderer,
                        new TextComponentTranslation("treechop.gui.settings.label.only_chop_trees_with_leaves"),
                        makeToggleSettingRow(SettingsField.TREES_MUST_HAVE_LEAVES)
                )
        );

        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative()) {
            optionRows.add(
                    new LabeledGui(fontRenderer,
                            new TextComponentTranslation("treechop.gui.settings.label.chop_in_creative_mode"),
                            makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE)
                    )
            );
        }

        return optionRows;
    }

    private LinkedList<NestedGui> makePageTwo() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        LinkedList<NestedGui> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledGui(fontRenderer,
                        new TextComponentTranslation("treechop.gui.settings.label.chop_in_creative_mode"),
                        makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE)
                )
        );

        optionRows.add(
                new LabeledGui(fontRenderer,
                        new TextComponentTranslation("treechop.gui.settings.label.chopping_indicator"),
                        new ToggleGui(
                                () -> Client.setChoppingIndicatorVisibility(!Client.isChoppingIndicatorEnabled()),
                                () -> ToggleWidget.State.of(Client.isChoppingIndicatorEnabled(), true)
                        )
                )
        );

        optionRows.add(
                new LabeledGui(fontRenderer,
                        new TextComponentTranslation("treechop.gui.settings.label.feedback_messages"),
                        new ToggleGui(
                                () -> ConfigHandler.CLIENT.showFeedbackMessages.set(!ConfigHandler.CLIENT.showFeedbackMessages.get()),
                                () -> ToggleWidget.State.of(ConfigHandler.CLIENT.showFeedbackMessages.get(), true)
                        )
                )
        );

        optionRows.add(
                new LabeledGui(fontRenderer,
                        new TextComponentTranslation("treechop.gui.settings.label.felling_options"),
                        new ToggleGui(
                                () -> ConfigHandler.CLIENT.showFellingOptions.set(!ConfigHandler.CLIENT.showFellingOptions.get()),
                                () -> ToggleWidget.State.of(
                                        ConfigHandler.CLIENT.showFellingOptions.get(),
                                        Client.getServerPermissions().isPermitted(new Setting(SettingsField.FELLING, false))
                                )
                        )
                )
        );

        return optionRows;
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return children;
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();
        KeyBindings.buttonPressed();
    }

    private SneakBehavior getNextSneakBehavior() {
        return Client.getChopSettings().getSneakBehavior() == SneakBehavior.NONE ? SneakBehavior.INVERT_CHOPPING : SneakBehavior.NONE;
    }

    private ToggleGui makeToggleSettingRow(SettingsField field) {
        return new ToggleGui(
                () -> Client.getChopSettings().set(field, !Client.getChopSettings().get(field, Boolean.class)),
                () -> ToggleWidget.State.of(
                        Client.getChopSettings().get(field, Boolean.class),
                        Client.getServerPermissions().isPermitted(new Setting(field, !Client.getChopSettings().get(field, Boolean.class)))
                )
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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (needToRebuild) {
            initGui();
            needToRebuild = false;
        }

        renderBackground();

        int listTop = getListTop();
        int listBottom = getListBottom();
        optionsList.setBox(0, listTop, width, listBottom - listTop);
        optionsList.render(mouseX, mouseY, partialTicks);
        doneButton.getBox().setTop(getDoneButtonTop());
        doneButton.render(mouseX, mouseY, partialTicks);
        String title = I18n.format("treechop.gui.settings.title", TreeChopMod.MOD_NAME);
        drawString(fontRenderer, title, (this.width - fontRenderer.getStringWidth(title)) / 2, getTitleTop(), 16777215);
        // TODO: check out ClientSettingsScreen.func_243293_a for draw reordering; might be important for tooltips
    }

    public void renderBackground() {
        drawGradientRect(0, 0, width, height, -1072689136, -804253680);
//        NestedGui.fill(0, 0, width, height, 0x00000080);
    }

    @Override
    public boolean doesGuiPauseGame() {
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

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return true;
    }

}
