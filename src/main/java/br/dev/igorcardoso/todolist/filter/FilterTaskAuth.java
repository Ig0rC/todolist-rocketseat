package br.dev.igorcardoso.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.dev.igorcardoso.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var serveletPath = request.getServletPath();

    if (serveletPath.startsWith("/tasks/")) {
      String authorization = request.getHeader("Authorization");

      String authEncoded = authorization.substring("Basic".length()).trim();

      byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

      String authString = new String(authDecoded);

      String[] credentials = authString.split(":");

      String username = credentials[0];
      String password = credentials[1];

      System.out.println(username + " " + password);

      var isExistsUsername = this.userRepository.findByUsername(username);

      if (isExistsUsername == null) {
        response.sendError(401);

        return;
      }

      var passwordVerify = BCrypt.verifyer()
          .verify(password.toCharArray(), isExistsUsername.getPassword());

      if (passwordVerify.verified == false) {
        response.sendError(401);

        return;
      }

      request.setAttribute("userId", isExistsUsername.getId());

      filterChain.doFilter(request, response);
    } else {
      filterChain.doFilter(request, response);

    }
  }
}
