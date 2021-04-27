package ht.treechop.common.config;

import ht.treechop.common.config.item.ItemIdentifier;
import ht.treechop.common.config.item.ItemNamespaceIdentifier;
import ht.treechop.common.config.item.ItemTagIdentifier;
import ht.treechop.common.config.item.MalformedItemIdentifier;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdentifierTest {

    @Test
    void fromDefaultNamespace() {
        ItemIdentifier id = ItemIdentifier.from("log");
        assertThat(id.getNamespace(), is("minecraft"));
        assertThat(id.getLocalSpace(), is("log"));
        assertThat(id.getQualifiers(), is(""));
    }

    @Test
    void fromExplicitNamespace() {
        ItemIdentifier id = ItemIdentifier.from("chimney:chute");
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chute"));
        assertThat(id.getQualifiers(), is(""));
    }

    @Test
    void fromIdWithQualifier() {
        ItemIdentifier id = ItemIdentifier.from("chimney:chute?nice");
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chute"));
        assertThat(id.getQualifiers(), is("?nice"));
    }

    @Test
    void fromMod() {
        ItemIdentifier id = ItemIdentifier.from("@chimney");
        assertTrue(id instanceof ItemNamespaceIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is(""));
        assertThat(id.getQualifiers(), is(""));
    }

    @Test
    void fromModWithQualifier() {
        ItemIdentifier id = ItemIdentifier.from("@chimney?nice");
        assertTrue(id instanceof ItemNamespaceIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is(""));
        assertThat(id.getQualifiers(), is("?nice"));
    }

    @Test
    void badModFormat() {
        ItemIdentifier id = ItemIdentifier.from("@chimney:chute");
        assertTrue(id instanceof MalformedItemIdentifier);
    }

    @Test
    void fromVanillaTag() {
        ItemIdentifier id = ItemIdentifier.from("#logs");
        assertTrue(id instanceof ItemTagIdentifier);
        assertThat(id.getNamespace(), is("minecraft"));
        assertThat(id.getLocalSpace(), is("logs"));
        assertThat(id.getQualifiers(), is(""));
    }

    @Test
    void fromVanillaTagWithQualifier() {
        ItemIdentifier id = ItemIdentifier.from("#logs?nice");
        assertTrue(id instanceof ItemTagIdentifier);
        assertThat(id.getNamespace(), is("minecraft"));
        assertThat(id.getLocalSpace(), is("logs"));
        assertThat(id.getQualifiers(), is("?nice"));
    }

    @Test
    void fromExplicitTag() {
        ItemIdentifier id = ItemIdentifier.from("#chimney:chutes");
        assertTrue(id instanceof ItemTagIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chutes"));
        assertThat(id.getQualifiers(), is(""));
    }

    @Test
    void fromExplicitTagWithQualifier() {
        ItemIdentifier id = ItemIdentifier.from("#chimney:chutes?nice");
        assertTrue(id instanceof ItemTagIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chutes"));
        assertThat(id.getQualifiers(), is("?nice"));
    }

}