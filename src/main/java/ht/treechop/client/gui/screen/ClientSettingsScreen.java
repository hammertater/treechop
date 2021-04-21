package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.client.gui.options.LabeledOptionRow;
import ht.treechop.client.gui.options.OptionList;
import ht.treechop.client.gui.options.OptionRow;
import ht.treechop.client.gui.options.ButtonOptionRow;
import ht.treechop.client.gui.options.ToggleOptionRow;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.StickyWidget;
import ht.treechop.client.gui.widget.ToggleWidget;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
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

    protected OptionList optionsRowList;
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
        Collection<OptionRow> optionRows = optionsPage == 0 ? makePageOne() : makePageTwo();

        optionRows.add(
                new ButtonOptionRow(
                        optionsPage == 0 ? Sprite.PAGE_ONE : Sprite.PAGE_TWO,
                        optionsPage == 0 ? Sprite.HIGHLIGHTED_PAGE_ONE : Sprite.HIGHLIGHTED_PAGE_TWO,
                        () -> {
                            needToRebuild = true;
                            optionsPage = optionsPage == 0 ? 1 : 0;
                        }
                )
        );

        setNumRows(optionRows.size());

        int listTop = getListTop();
        int listBottom = getListBottom();
        this.optionsRowList = addListener(new OptionList(
                minecraft,
                width,
                listTop,
                listBottom,
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

    private LinkedList<OptionRow> makePageOne() {
        LinkedList<OptionRow> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledOptionRow(font,
                        new TranslationTextComponent("treechop.gui.settings.label.chopping"),
                        makeToggleSettingRow(SettingsField.CHOPPING)
                )
        );

//        optionRows.add(
//                new LabeledOptionRow(font,
//                        new TranslationTextComponent("treechop.gui.settings.label.felling"),
//                        makeToggleSettingRow(SettingsField.FELLING)
//                )
//        );

        optionRows.add(
                new LabeledOptionRow(font,
                        new TranslationTextComponent("treechop.gui.settings.label.sneaking_inverts_chopping"),
                        new ToggleOptionRow(
                                () -> Client.getChopSettings().setSneakBehavior(getNextSneakBehavior()),
                                () -> ToggleWidget.State.of(
                                        Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                                        isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, getNextSneakBehavior())
                                )
                        )
                )
        );

//        optionRows.add(
//                new LabeledOptionRow(font,
//                        new TranslationTextComponent("treechop.gui.settings.label.sneaking_inverts"),
//                        new ExclusiveOptionRow.Builder()
//                                .add(
//                                        new TranslationTextComponent("treechop.gui.settings.button.chopping"),
//                                        () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_CHOPPING),
//                                        () -> StickyWidget.State.of(
//                                                Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
//                                                isSettingPermitted(SettingsField.CHOPPING, !Client.getChopSettings().getChoppingEnabled())
//                                                        && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_CHOPPING)
//                                        )
//                                )
//                                .add(
//                                        new TranslationTextComponent("treechop.gui.settings.button.felling"),
//                                        () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_FELLING),
//                                        () -> StickyWidget.State.of(
//                                                Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_FELLING,
//                                                isSettingPermitted(SettingsField.FELLING, !Client.getChopSettings().getFellingEnabled())
//                                                        && isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.INVERT_FELLING)
//                                        )
//                                )
//                                .add(
//                                        new TranslationTextComponent("treechop.gui.settings.button.nothing"),
//                                        () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.NONE),
//                                        () -> makeStickyWidgetState(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.NONE)
//                                )
//                                .build()
//                )
//        );

        optionRows.add(
                new LabeledOptionRow(font,
                        new TranslationTextComponent("treechop.gui.settings.label.only_chop_trees_with_leaves"),
                        makeToggleSettingRow(SettingsField.TREES_MUST_HAVE_LEAVES)
                )
        );

        return optionRows;
    }

    private LinkedList<OptionRow> makePageTwo() {
        LinkedList<OptionRow> optionRows = new LinkedList<>();

        optionRows.add(
                new LabeledOptionRow(font,
                        new TranslationTextComponent("treechop.gui.settings.label.chop_in_creative_mode"),
                        makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE)
                )
        );

        optionRows.add(
                new LabeledOptionRow(font,
                        new TranslationTextComponent("treechop.gui.settings.label.chopping_indicator"),
                        new ToggleOptionRow(
                                () -> Client.setChoppingIndicatorVisibility(!Client.isChoppingIndicatorEnabled()),
                                () -> ToggleWidget.State.of(Client.isChoppingIndicatorEnabled(), true)
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

    private ToggleOptionRow makeToggleSettingRow(SettingsField field) {
        return new ToggleOptionRow(
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

        doneButton.y = getDoneButtonTop();

        this.renderBackground(matrixStack);
        optionsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
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
        return OptionList.getHeightForRows(numRows, ROW_HEIGHT);
    }

    protected int getListBottom() {
        return getMiddleY() + getListHeight() / 2;
    }

    protected int getDoneButtonTop() {
        return getListBottom() + SPACE_ABOVE_AND_BELOW_LIST;
    }
}
