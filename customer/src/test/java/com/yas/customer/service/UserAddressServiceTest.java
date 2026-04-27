package com.yas.customer.service;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.UserAddress;
import com.yas.customer.repository.UserAddressRepository;
import com.yas.customer.utils.Constants;
import com.yas.customer.viewmodel.address.ActiveAddressVm;
import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import com.yas.customer.viewmodel.useraddress.UserAddressVm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String USER_ID          = "user-123";
    private static final String ANONYMOUS_USER   = "anonymousUser";
    private static final Long   ADDRESS_ID_1     = 1L;
    private static final Long   ADDRESS_ID_2     = 2L;

    // ── Mocks ─────────────────────────────────────────────────────────────────
    @Mock private UserAddressRepository userAddressRepository;
    @Mock private LocationService       locationService;

    @InjectMocks
    private UserAddressService userAddressService;

    // ── Security context helpers ───────────────────────────────────────────────

    private void authenticateAs(String userId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @BeforeEach
    void authenticateAsUser() {
        authenticateAs(USER_ID);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private UserAddress activeUserAddress(Long addressId) {
        return UserAddress.builder()
            .userId(USER_ID)
            .addressId(addressId)
            .isActive(true)
            .build();
    }

    private UserAddress inactiveUserAddress(Long addressId) {
        return UserAddress.builder()
            .userId(USER_ID)
            .addressId(addressId)
            .isActive(false)
            .build();
    }

    private AddressDetailVm addressDetailVm(Long id) {
        return new AddressDetailVm(
            id, "Contact", "0123456789", "123 Street",
            "City", "70000", 1L, "District",
            1L, "Province", 1L, "Country"
        );
    }

    private AddressPostVm addressPostVm() {
        return new AddressPostVm(
            "Contact", "0123456789", "123 Street",
            "City", "70000", 1L, 1L, 1L
        );
    }

    private AddressVm addressVm(Long id) {
        return new AddressVm(id, "Contact", "0123456789", "123 Street",
            "City", "70000", 1L, 1L, 1L);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserAddressList()")
    class GetUserAddressListTests {

        @Test
        @DisplayName("given anonymous user, throws AccessDeniedException")
        void givenAnonymousUser_whenGetUserAddressList_thenThrowAccessDeniedException() {
            authenticateAs(ANONYMOUS_USER);

            assertThatThrownBy(() -> userAddressService.getUserAddressList())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(Constants.ErrorCode.UNAUTHENTICATED);

            verifyNoInteractions(userAddressRepository, locationService);
        }

        @Test
        @DisplayName("given no saved addresses, returns empty list")
        void givenNoAddresses_whenGetUserAddressList_thenReturnEmptyList() {
            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());
            when(locationService.getAddressesByIdList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<ActiveAddressVm> result = userAddressService.getUserAddressList();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("given multiple addresses, returns list sorted active-first")
        void givenMultipleAddresses_whenGetUserAddressList_thenReturnSortedActiveFirst() {
            UserAddress inactive = inactiveUserAddress(ADDRESS_ID_1);
            UserAddress active   = activeUserAddress(ADDRESS_ID_2);

            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(inactive, active));
            when(locationService.getAddressesByIdList(anyList()))
                .thenReturn(List.of(addressDetailVm(ADDRESS_ID_1), addressDetailVm(ADDRESS_ID_2)));

            List<ActiveAddressVm> result = userAddressService.getUserAddressList();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).isActive()).isTrue();   // active address first
            assertThat(result.get(1).isActive()).isFalse();
        }

        @Test
        @DisplayName("given single address, returns it correctly mapped")
        void givenSingleAddress_whenGetUserAddressList_thenReturnMappedActiveAddressVm() {
            UserAddress userAddress = activeUserAddress(ADDRESS_ID_1);
            AddressDetailVm detail  = addressDetailVm(ADDRESS_ID_1);

            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(userAddress));
            when(locationService.getAddressesByIdList(List.of(ADDRESS_ID_1))).thenReturn(List.of(detail));

            List<ActiveAddressVm> result = userAddressService.getUserAddressList();

            assertThat(result).hasSize(1);
            ActiveAddressVm vm = result.get(0);
            assertThat(vm.id()).isEqualTo(ADDRESS_ID_1);
            assertThat(vm.contactName()).isEqualTo(detail.contactName());
            assertThat(vm.phone()).isEqualTo(detail.phone());
            assertThat(vm.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getAddressDefault()")
    class GetAddressDefaultTests {

        @Test
        @DisplayName("given anonymous user, throws AccessDeniedException")
        void givenAnonymousUser_whenGetAddressDefault_thenThrowAccessDeniedException() {
            authenticateAs(ANONYMOUS_USER);

            assertThatThrownBy(() -> userAddressService.getAddressDefault())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining(Constants.ErrorCode.UNAUTHENTICATED);

            verifyNoInteractions(userAddressRepository, locationService);
        }

        @Test
        @DisplayName("given no active address, throws NotFoundException")
        void givenNoActiveAddress_whenGetAddressDefault_thenThrowNotFoundException() {
            when(userAddressRepository.findByUserIdAndIsActiveTrue(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userAddressService.getAddressDefault())
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User address not found");

            verifyNoInteractions(locationService);
        }

        @Test
        @DisplayName("given active address exists, returns mapped AddressDetailVm")
        void givenActiveAddress_whenGetAddressDefault_thenReturnAddressDetailVm() {
            UserAddress   userAddress = activeUserAddress(ADDRESS_ID_1);
            AddressDetailVm expected  = addressDetailVm(ADDRESS_ID_1);

            when(userAddressRepository.findByUserIdAndIsActiveTrue(USER_ID))
                .thenReturn(Optional.of(userAddress));
            when(locationService.getAddressById(ADDRESS_ID_1)).thenReturn(expected);

            AddressDetailVm result = userAddressService.getAddressDefault();

            assertThat(result.id()).isEqualTo(ADDRESS_ID_1);
            assertThat(result.contactName()).isEqualTo(expected.contactName());
            verify(locationService).getAddressById(ADDRESS_ID_1);
        }
    }

    @Nested
    @DisplayName("createAddress()")
    class CreateAddressTests {

        @Test
        @DisplayName("given user's first address, sets isActive=true")
        void givenFirstAddress_whenCreateAddress_thenSavesWithIsActiveTrue() {
            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

            AddressVm     created     = addressVm(ADDRESS_ID_1);
            UserAddress   saved       = activeUserAddress(ADDRESS_ID_1);
            when(locationService.createAddress(any(AddressPostVm.class))).thenReturn(created);
            when(userAddressRepository.save(any(UserAddress.class))).thenReturn(saved);

            UserAddressVm result = userAddressService.createAddress(addressPostVm());

            ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
            verify(userAddressRepository).save(captor.capture());

            assertThat(captor.getValue().getIsActive()).isTrue();
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("given user already has addresses, sets isActive=false for new one")
        void givenExistingAddresses_whenCreateAddress_thenSavesWithIsActiveFalse() {
            when(userAddressRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(activeUserAddress(ADDRESS_ID_1)));

            AddressVm   created = addressVm(ADDRESS_ID_2);
            UserAddress saved   = inactiveUserAddress(ADDRESS_ID_2);
            when(locationService.createAddress(any(AddressPostVm.class))).thenReturn(created);
            when(userAddressRepository.save(any(UserAddress.class))).thenReturn(saved);

            userAddressService.createAddress(addressPostVm());

            ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
            verify(userAddressRepository).save(captor.capture());

            assertThat(captor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("given valid request, saves address with correct userId and addressId")
        void givenValidRequest_whenCreateAddress_thenSavesCorrectUserAddress() {
            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

            AddressVm   created = addressVm(ADDRESS_ID_1);
            UserAddress saved   = activeUserAddress(ADDRESS_ID_1);
            when(locationService.createAddress(any())).thenReturn(created);
            when(userAddressRepository.save(any(UserAddress.class))).thenReturn(saved);

            userAddressService.createAddress(addressPostVm());

            ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
            verify(userAddressRepository).save(captor.capture());

            assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
            assertThat(captor.getValue().getAddressId()).isEqualTo(ADDRESS_ID_1);
        }
    }

    @Nested
    @DisplayName("deleteAddress()")
    class DeleteAddressTests {

        @Test
        @DisplayName("given address not found for user, throws NotFoundException")
        void givenAddressNotFound_whenDeleteAddress_thenThrowNotFoundException() {
            when(userAddressRepository.findOneByUserIdAndAddressId(USER_ID, ADDRESS_ID_1)).thenReturn(null);

            assertThatThrownBy(() -> userAddressService.deleteAddress(ADDRESS_ID_1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User address not found");

            verify(userAddressRepository, never()).delete(any());
        }

        @Test
        @DisplayName("given valid address belonging to user, deletes it")
        void givenValidAddress_whenDeleteAddress_thenDeletesFromRepository() {
            UserAddress userAddress = activeUserAddress(ADDRESS_ID_1);
            when(userAddressRepository.findOneByUserIdAndAddressId(USER_ID, ADDRESS_ID_1))
                .thenReturn(userAddress);

            userAddressService.deleteAddress(ADDRESS_ID_1);

            verify(userAddressRepository).delete(userAddress);
        }
    }

    @Nested
    @DisplayName("chooseDefaultAddress()")
    class ChooseDefaultAddressTests {

        @Test
        @DisplayName("given target addressId, sets only that address to isActive=true")
        void givenTargetAddressId_whenChooseDefault_thenOnlyTargetIsActive() {
            UserAddress addr1 = activeUserAddress(ADDRESS_ID_1);   // currently active
            UserAddress addr2 = inactiveUserAddress(ADDRESS_ID_2); // not active yet

            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(addr1, addr2));

            userAddressService.chooseDefaultAddress(ADDRESS_ID_2);

            assertThat(addr1.getIsActive()).isFalse();  // deactivated
            assertThat(addr2.getIsActive()).isTrue();   // now active

            verify(userAddressRepository).saveAll(List.of(addr1, addr2));
        }

        @Test
        @DisplayName("given already-active address re-selected, remains active; others deactivated")
        void givenCurrentlyActiveReselected_whenChooseDefault_thenRemainsActive() {
            UserAddress addr1 = activeUserAddress(ADDRESS_ID_1);
            UserAddress addr2 = activeUserAddress(ADDRESS_ID_2); // incorrectly both active

            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(addr1, addr2));

            userAddressService.chooseDefaultAddress(ADDRESS_ID_1);

            assertThat(addr1.getIsActive()).isTrue();
            assertThat(addr2.getIsActive()).isFalse();
            verify(userAddressRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("given user has no addresses, saves empty list without error")
        void givenNoAddresses_whenChooseDefault_thenSavesEmptyListGracefully() {
            when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

            userAddressService.chooseDefaultAddress(ADDRESS_ID_1);

            verify(userAddressRepository).saveAll(Collections.emptyList());
        }
    }
}