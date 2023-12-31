package br.com.marcussilva.todolist.filters;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import br.com.marcussilva.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    String servletPath = request.getServletPath();

    if (servletPath.contains("/tasks")) {
      String auth = request.getHeader("Authorization");

      String authEncoded = auth.substring("Basic".length()).trim();

      byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

      String authString = new String(authDecoded);
      String[] credentials = authString.split(":");
      String username = credentials[0];
      String password = credentials[1];

      var user = this.userRepository.findByUsername(username);

      if (user == null) {
        response.sendError(401, "Unauthorized");
      } else {
        Result passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

        if (passwordVerify.verified) {
          request.setAttribute("idUser", user.getId());
          filterChain.doFilter(request, response);
        } else {
          response.sendError(401, "Unauthorized");
        }
      }
    } else {
      filterChain.doFilter(request, response);
    }
  }
}
