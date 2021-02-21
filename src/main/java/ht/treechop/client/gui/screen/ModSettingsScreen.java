package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.client.gui.options.ExclusiveOptionRow;
import ht.treechop.client.gui.options.LabeledOptionRow;
import ht.treechop.client.gui.options.OptionList;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.common.config.SneakBehavior;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class ModSettingsScreen extends Screen {

    private static final int TOP_Y = 32;
    private static final int BOTTOM_Y_OFFSET = 32;
    private static final int ROW_HEIGHT = 25;

    protected OptionList optionsRowList;

    public ModSettingsScreen() {
        super(new TranslationTextComponent("treechop.gui.settings.title", TreeChopMod.MOD_NAME));
    }

    @Override
    protected void init() {
        super.init();

        this.optionsRowList = addListener(new OptionList(
                minecraft,
                width,
                height,
                TOP_Y,
                height - BOTTOM_Y_OFFSET,
                ROW_HEIGHT
        ));

        optionsRowList.addRow(
                new LabeledOptionRow(
                        font,
                        new TranslationTextComponent("treechop.gui.settings.label.chopping"),
                        new ExclusiveOptionRow.Builder()
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.on"),
                                        () -> Client.getChopSettings().setChoppingEnabled(true),
                                        () -> Client.getChopSettings().getChoppingEnabled()
                                )
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.off"),
                                        () -> Client.getChopSettings().setChoppingEnabled(false),
                                        () -> !Client.getChopSettings().getChoppingEnabled()
                                )
                                .build()
                )
        );

        this.optionsRowList.addRow(
                new LabeledOptionRow(
                        font,
                        new TranslationTextComponent("treechop.gui.settings.label.felling"),
                        new ExclusiveOptionRow.Builder()
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.on"),
                                        () -> Client.getChopSettings().setFellingEnabled(true),
                                        () -> Client.getChopSettings().getFellingEnabled()
                                )
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.off"),
                                        () -> Client.getChopSettings().setFellingEnabled(false),
                                        () -> !Client.getChopSettings().getFellingEnabled()
                                )
                                .build()
                )
        );

        this.optionsRowList.addRow(
                new LabeledOptionRow(
                        font,
                        new TranslationTextComponent("treechop.gui.settings.label.sneaking_inverts"),
                        new ExclusiveOptionRow.Builder()
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.chopping"),
                                        () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_CHOPPING),
                                        () -> Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING
                                )
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.felling"),
                                        () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.INVERT_FELLING),
                                        () -> Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_FELLING
                                )
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.nothing"),
                                        () -> Client.getChopSettings().setSneakBehavior(SneakBehavior.NONE),
                                        () -> Client.getChopSettings().getSneakBehavior() == SneakBehavior.NONE
                                )
                                .build()
                )
        );

        this.optionsRowList.addRow(
                new LabeledOptionRow(
                        font,
                        new TranslationTextComponent("treechop.gui.settings.label.only_chop_trees_with_leaves"),
                        new ExclusiveOptionRow.Builder()
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.yes"),
                                        () -> Client.getChopSettings().setTreesMustHaveLeaves(true),
                                        () -> Client.getChopSettings().getTreesMustHaveLeaves()
                                )
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.no"),
                                        () -> Client.getChopSettings().setTreesMustHaveLeaves(false),
                                        () -> !Client.getChopSettings().getTreesMustHaveLeaves()
                                )
                                .build()
                )
        );

        this.optionsRowList.addRow(
                new LabeledOptionRow(
                        font,
                        new TranslationTextComponent("treechop.gui.settings.label.chop_in_creative_mode"),
                        new ExclusiveOptionRow.Builder()
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.yes"),
                                        () -> Client.getChopSettings().setChopInCreativeMode(true),
                                        () -> Client.getChopSettings().getChopInCreativeMode()
                                )
                                .add(
                                        new TranslationTextComponent("treechop.gui.settings.button.no"),
                                        () -> Client.getChopSettings().setChopInCreativeMode(false),
                                        () -> !Client.getChopSettings().getChopInCreativeMode()
                                )
                                .build()
                )
        );

        final int doneButtonWidth = 200;
        addButton(new Button(
                (doneButtonWidth - 200) / 2,
                height - 26,
                doneButtonWidth,
                GUIUtil.BUTTON_HEIGHT,
                ITextComponent.getTextComponentOrEmpty(I18n.format("gui.done")),
                button -> closeScreen()
        ));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        optionsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        // TODO: check out ModSettingsScreen.func_243293_a for draw reordering; might be important for tooltips
    }
}
