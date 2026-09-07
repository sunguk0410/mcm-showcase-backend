package likelion.mcmshowcase.member.service;

import likelion.mcmshowcase.global.exception.CustomException;
import likelion.mcmshowcase.member.entity.Member;
import likelion.mcmshowcase.member.entity.MemberWishlist;
import likelion.mcmshowcase.member.repository.MemberRepository;
import likelion.mcmshowcase.member.repository.MemberWishlistRepository;
import likelion.mcmshowcase.product.entity.Product;
import likelion.mcmshowcase.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberWishlistServiceTest {
    @Mock MemberRepository members;
    @Mock ProductRepository products;
    @Mock MemberWishlistRepository wishlists;
    @Mock Member member;
    @Mock Product product;
    MemberWishlistService service;

    @BeforeEach
    void setUp() {
        service = new MemberWishlistService(members, products, wishlists);
    }

    private void existingMemberAndProduct() {
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(products.findById(2L)).thenReturn(Optional.of(product));
    }

    @Test
    void repeatedAddDoesNotCreateDuplicate() {
        existingMemberAndProduct();
        when(wishlists.existsByMemberAndProduct(member, product)).thenReturn(false, true);
        service.add(1L, 2L);
        service.add(1L, 2L);
        verify(wishlists, times(1)).save(argThat(item ->
                item.getMember() == member && item.getProduct() == product));
    }

    @Test
    void repeatedRemoveSucceeds() {
        existingMemberAndProduct();
        MemberWishlist item = MemberWishlist.create(member, product, LocalDateTime.now());
        when(wishlists.findByMemberAndProduct(member, product))
                .thenReturn(Optional.of(item), Optional.empty());
        service.remove(1L, 2L);
        service.remove(1L, 2L);
        verify(wishlists, times(1)).delete(item);
    }

    @Test
    void emptyWishlistReturnsEmptyItems() {
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(wishlists.findByMemberOrderByCreatedAtDescIdDesc(member)).thenReturn(List.of());
        assertTrue(service.getWishlist(1L).items().isEmpty());
    }

    @Test
    void wishlistReturnsProductInformation() {
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(product.getId()).thenReturn(2L);
        when(product.getName()).thenReturn("Bag");
        when(wishlists.findByMemberOrderByCreatedAtDescIdDesc(member))
                .thenReturn(List.of(MemberWishlist.create(member, product, LocalDateTime.now())));
        var items = service.getWishlist(1L).items();
        assertEquals(1, items.size());
        assertEquals(2L, items.get(0).productId());
        assertEquals("Bag", items.get(0).name());
    }

    @Test
    void unknownMemberCannotReadWishlist() {
        when(members.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> service.getWishlist(1L));
        verifyNoInteractions(wishlists);
    }

    @Test
    void unknownProductCannotBeAdded() {
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(products.findById(2L)).thenReturn(Optional.empty());
        assertThrows(CustomException.class, () -> service.add(1L, 2L));
        verifyNoInteractions(wishlists);
    }
}
