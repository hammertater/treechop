package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.client.gui.options.ExclusiveButtonsGui;
import ht.treechop.client.gui.options.LabeledGui;
import ht.treechop.client.gui.options.RowsGui;
import ht.treechop.client.gui.options.NestedGui;
import ht.treechop.client.gui.options.ButtonGui;
import ht.treechop.client.gui.options.ToggleGui;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.StickyWidget;
import ht.treechop.client.gui.widget.ToggleWidget;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.LinkedList;

public class ClientSettingsScreen extends Screen {

    private static final int ROW_HEIGHT = GUIUtil.BUTTON_HEIGHT + 1;
    private static final int INSET_SIZE = 20;
    private static final boolean IS_PAUSE_SCREEN = true;
    private static final int SPACE_ABOVE_AND_BELOW_LIST = 20;

    protected RowsGui optionsList;
    private Button doneButton;
    private int optionsPage = 0;
    private int numRows = 0;
    private boolean needToRebuild = false;

    public ClientSettingsScreen() {
        super(new TranslationTextComponent("treechop.gui.settings.title", TreeChopMod.MOD_NAME));
    }

    @Override
    protected void init() {
        super.init();
        rebuild();
    }

    private void rebuild() {
        Collection<NestedGui> optionRows = optionsPage == 0 ? makePageOne() : makePageTwo();

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

        setNumRows(optionRows.size());

        this.optionsList = addListener(new RowsGui(
                minecraft,
                ROW_HEIGHT,
                optionRows
        ));

        final int doneButtonWidth = 200;
        doneButton = addButton(new Button(
                (width - doneButtonWidth) / 2,
                getDoneButtonTop(),
                doneButtonWidth,
                GUIUtil.BUTTON_HEIGHT,
                ITextComponent.getTextComponentOrEmpty(I18n.format("gui.done")),
                button -> closeScreen()
        ));
    }

    private LinkedList<NestedGui> makePageOne() {
        LinkedList<NestedGui> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledGui(font,
                        new TranslationTextComponent("treechop.gui.settings.label.chopping"),
                        makeToggleSettingRow(SettingsField.CHOPPING)
                )
        );

        if (ConfigHandler.CLIENT.showFellingOptions.get()) {
            optionRows.add(
                    new LabeledGui(font,
                            new TranslationTextComponent("treechop.gui.settings.label.felling"),
                            makeToggleSettingRow(SettingsField.FELLING)
                    )
            );

            optionRows.add(
                    new LabeledGui(font,
                            new TranslationTextComponent("treechop.gui.settings.label.sneaking_inverts"),
                            new ExclusiveButtonsGui.Builder()
                                    .add(
                                            new TranslationTextComponent("treechop.gui.settings.button.chopping"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_CHOPPING),
                                            () -> StickyWidget.State.of(
                                                    Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                                                    isSettingPermitted(SettingsField.CHOPPING, !Client.getChopSettings().getChoppingEnabled())
                                                            && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_CHOPPING)
                                            )
                                    )
                                    .add(
                                            new TranslationTextComponent("treechop.gui.settings.button.felling"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_FELLING),
                                            () -> StickyWidget.State.of(
                                                    Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_FELLING,
                                                    isSettingPermitted(SettingsField.FELLING, !Client.getChopSettings().getFellingEnabled())
                                                            && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_FELLING)
                                            )
                                    )
                                    .add(
                                            new TranslationTextComponent("treechop.gui.settings.button.nothing"),
                                            () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.NONE),
                                            () -> makeStickyWidgetState(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.NONE)
                                    )
                                    .build()
                    )
            );
        }
        else {
            optionRows.add(
                    new LabeledGui(font,
                            new TranslationTextComponent("treechop.gui.settings.label.sneaking_inverts_chopping"),
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
                new LabeledGui(font,
                        new TranslationTextComponent("treechop.gui.settings.label.only_chop_trees_with_leaves"),
                        makeToggleSettingRow(SettingsField.TREES_MUST_HAVE_LEAVES)
                )
        );
;
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            optionRows.add(
                    new LabeledGui(font,
                            new TranslationTextComponent("treechop.gui.settings.label.chop_in_creative_mode"),
                            makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE)
                    )
            );
        }

        return optionRows;
    }

    private LinkedList<NestedGui> makePageTwo() {
        LinkedList<NestedGui> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledGui(font,
                        new TranslationTextComponent("treechop.gui.settings.label.chop_in_creative_mode"),
                        makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE)
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        new TranslationTextComponent("treechop.gui.settings.label.chopping_indicator"),
                        new ToggleGui(
                                () -> Client.setChoppingIndicatorVisibility(!Client.isChoppingIndicatorEnabled()),
                                () -> ToggleWidget.State.of(Client.isChoppingIndicatorEnabled(), true)
                        )
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        new TranslationTextComponent("treechop.gui.settings.label.feedback_messages"),
                        new ToggleGui(
                                () -> ConfigHandler.CLIENT.showFeedbackMessages.set(!ConfigHandler.CLIENT.showFeedbackMessages.get()),
                                () -> ToggleWidget.State.of(ConfigHandler.CLIENT.showFeedbackMessages.get(), true)
                        )
                )
        );

        optionRows.add(
                new LabeledGui(font,
                        new TranslationTextComponent("treechop.gui.settings.label.felling_options"),
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

    private void setNumRows(int numRows) {
        this.numRows = numRows;
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (needToRebuild) {
            children.clear();
            buttons.clear();
            rebuild();
            needToRebuild = false;
        }

        renderBackground(matrixStack);

        doneButton.y = getDoneButtonTop();

        int listTop = getListTop();
        int listBottom = getListBottom();
        optionsList.setBox(0, listTop, width, listBottom - listTop);
        optionsList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, getTitleTop(), 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        // TODO: check out ClientSettingsScreen.func_243293_a for draw reordering; might be important for tooltips
    }

    @Override
    public void renderBackground(MatrixStack matrixStack) {
        super.renderBackground(matrixStack);
        fill(matrixStack, INSET_SIZE, INSET_SIZE, width - INSET_SIZE, height - INSET_SIZE, 0x00000080);
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
        return RowsGui.getHeightForRows(numRows, ROW_HEIGHT);
    }

    protected int getListBottom() {
        return getMiddleY() + getListHeight() / 2;
    }

    protected int getDoneButtonTop() {
        return getListBottom() + SPACE_ABOVE_AND_BELOW_LIST;
    }
}
