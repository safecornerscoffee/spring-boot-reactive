package io.safecorners.springbootreactive.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ItemUnitTest {

    @Test
    void itemBasicShouldWork() {
        Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);

        assertThat(sampleItem.getId()).isEqualTo("item1");
        assertThat(sampleItem.getName()).isEqualTo("TV tray");
        assertThat(sampleItem.getDescription()).isEqualTo("Alf TV tray");
        assertThat(sampleItem.getPrice()).isEqualTo(19.99);

        Item sampleItem2 = new Item("item1", "TV tray", "Alf TV tray", 19.99);

        assertThat(sampleItem).isEqualTo(sampleItem2);
    }
}