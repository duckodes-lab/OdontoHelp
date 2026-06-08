package br.com.odontohelp.fiscal.tenant;

import br.com.odontohelp.fiscal.config.JwtAuthFilter;
import br.com.odontohelp.fiscal.exception.AcessoNegadoException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantResolverTest {

    @Mock private HttpServletRequest request;

    @Test
    void jwtTenantPrevalece() {
        when(request.getAttribute(JwtAuthFilter.ATTR_TENANT_ID)).thenReturn("12345678000199");
        assertThat(TenantResolver.resolver(request, null)).isEqualTo("12345678000199");
    }

    @Test
    void mismatchRetorna403() {
        when(request.getAttribute(JwtAuthFilter.ATTR_TENANT_ID)).thenReturn("111");
        assertThatThrownBy(() -> TenantResolver.resolver(request, "222"))
                .isInstanceOf(AcessoNegadoException.class);
    }
}
