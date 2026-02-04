package com.itorly.rph.realtime;

import com.itorly.rph.organization.OrganizationMemberRepository;
import com.itorly.rph.project.Project;
import com.itorly.rph.project.ProjectRepository;
import com.itorly.rph.security.CustomUserDetailsService;
import com.itorly.rph.security.JwtTokenProvider;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String PROJECT_TOPIC_PREFIX = "/topic/projects/";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ProjectRepository projectRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public WebSocketAuthChannelInterceptor(
            JwtTokenProvider tokenProvider,
            CustomUserDetailsService userDetailsService,
            ProjectRepository projectRepository,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository
    ) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication authentication = authenticate(accessor);
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Authentication authentication = resolveAuthentication(accessor);
            validateSubscription(accessor, authentication);
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            validateSendDestination(accessor);
        }

        return message;
    }

    private Authentication authenticate(StompHeaderAccessor accessor) {
        String authHeader = resolveAuthHeader(accessor);
        if (!StringUtils.hasText(authHeader)) {
            throw new AccessDeniedException("Missing Authorization header");
        }

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        try {
            String email = tokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AccessDeniedException("Invalid token");
        }
    }

    private Authentication resolveAuthentication(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Unauthenticated WebSocket session");
        }
        return authentication;
    }

    private void validateSubscription(StompHeaderAccessor accessor, Authentication authentication) {
        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination) || !destination.startsWith(PROJECT_TOPIC_PREFIX)) {
            return;
        }

        Long projectId = parseProjectId(destination);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        memberRepository
                .findByOrganizationIdAndUserId(project.getOrganization().getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this organization"));
    }

    private void validateSendDestination(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (StringUtils.hasText(destination) && destination.startsWith(PROJECT_TOPIC_PREFIX)) {
            throw new AccessDeniedException("Sending to project topics is not permitted");
        }
    }

    private Long parseProjectId(String destination) {
        String raw = destination.substring(PROJECT_TOPIC_PREFIX.length());
        if (!StringUtils.hasText(raw)) {
            throw new AccessDeniedException("Missing project id");
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid project id");
        }
    }

    private String resolveAuthHeader(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(header)) {
            header = accessor.getFirstNativeHeader("authorization");
        }
        return header;
    }
}
