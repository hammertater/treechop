package ht.treechop.common.config;

import ht.treechop.common.config.resource.MalformedResourceIdentifier;
import ht.treechop.common.config.resource.ResourceIdentifier;
import ht.treechop.common.config.resource.ResourceNamespaceIdentifier;
import ht.treechop.common.config.resource.ResourceTagIdentifier;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdentifierTest {

    @Test
    void fromDefaultNamespace() {
        ResourceIdentifier id = ResourceIdentifier.from("log");
        assertThat(id.getNamespace(), is("minecraft"));
        assertThat(id.getLocalSpace(), is("log"));
        assertThat(id.getQualifiers().size(), is(0));
    }

    @Test
    void fromExplicitNamespace() {
        ResourceIdentifier id = ResourceIdentifier.from("chimney:chute");
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chute"));
        assertThat(id.getQualifiers().size(), is(0));
    }

    @Test
    void fromIdWithQualifier() {
        ResourceIdentifier id = ResourceIdentifier.from("chimney:chute?nice");
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chute"));
        assertThat(id.getQualifiers().size(), is(1));
        assertTrue(id.hasQualifier("nice"));
    }

    @Test
    void fromMod() {
        ResourceIdentifier id = ResourceIdentifier.from("@chimney");
        assertTrue(id instanceof ResourceNamespaceIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is(""));
        assertThat(id.getQualifiers().size(), is(0));
    }

    @Test
    void fromModWithQualifier() {
        ResourceIdentifier id = ResourceIdentifier.from("@chimney?nice");
        assertTrue(id instanceof ResourceNamespaceIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is(""));
        assertThat(id.getQualifiers().size(), is(1));
        assertTrue(id.hasQualifier("nice"));
    }

    @Test
    void badModFormat() {
        ResourceIdentifier id = ResourceIdentifier.from("@chimney:chute");
        assertTrue(id instanceof MalformedResourceIdentifier);
    }

    @Test
    void fromVanillaTag() {
        ResourceIdentifier id = ResourceIdentifier.from("#logs");
        assertTrue(id instanceof ResourceTagIdentifier);
        assertThat(id.getNamespace(), is("minecraft"));
        assertThat(id.getLocalSpace(), is("logs"));
        assertThat(id.getQualifiers().size(), is(0));
    }

    @Test
    void fromVanillaTagWithQualifier() {
        ResourceIdentifier id = ResourceIdentifier.from("#logs?nice");
        assertTrue(id instanceof ResourceTagIdentifier);
        assertThat(id.getNamespace(), is("minecraft"));
        assertThat(id.getLocalSpace(), is("logs"));
        assertThat(id.getQualifiers().size(), is(1));
        assertTrue(id.hasQualifier("nice"));
    }

    @Test
    void fromExplicitTag() {
        ResourceIdentifier id = ResourceIdentifier.from("#chimney:chutes");
        assertTrue(id instanceof ResourceTagIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chutes"));
        assertThat(id.getQualifiers().size(), is(0));
    }

    @Test
    void fromExplicitTagWithQualifier() {
        ResourceIdentifier id = ResourceIdentifier.from("#chimney:chutes?nice,horse=stallion");
        assertTrue(id instanceof ResourceTagIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chutes"));
        assertThat(id.getQualifiers().size(), is(2));
        assertTrue(id.hasQualifier("nice"));
        assertThat(id.getQualifier("horse").orElse(null), is("stallion"));
    }

    @Test
    void fromOverride() {
        ResourceIdentifier id = ResourceIdentifier.from("#chimney:chutes?chops=3,override=always");
        assertTrue(id instanceof ResourceTagIdentifier);
        assertThat(id.getNamespace(), is("chimney"));
        assertThat(id.getLocalSpace(), is("chutes"));
        assertThat(id.getQualifiers().size(), is(2));
        assertThat(id.getQualifier("chops").orElse(null), is("3"));
        assertThat(id.getQualifier("override").orElse(null), is("always"));
    }

}