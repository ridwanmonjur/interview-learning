// https://www.baeldung.com/registration-verify-user-by-email

@Autowired
ApplicationEventPublisher eventPublisher

@PostMapping("/user/registration")
public User registerUserAccount(
  @RequestBody @Valid UserDto userDto,
  final HttpServletRequest request
) { 
    
    try {
        // save user in database
        User registered = userService.registerNewUserAccount(userDto);  
        // smtp email
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
    } catch (UserAlreadyExistException uaeEx) {
       // ...
    } catch (RuntimeException ex) {
        // ...
    }
    return registered;
}

@Data
@AllArgsConstructor
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final String appUrl;
    private final Locale locale;
    private final User user;
    public OnRegistrationCompleteEvent(final User user, final Locale locale, final String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    ...
    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        final User user = event.getUser();
        final String token = UUID.randomUUID().toString();
        service.createVerificationTokenForUser(user, token);
        final SimpleMailMessage email = constructEmailMessage(event, user, token);
        mailSender.send(email);
    }

    private SimpleMailMessage constructEmailMessage(final OnRegistrationCompleteEvent event, final User user, final String token) {
        // ...
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm?token=" + token;
        // ...
    }
    

}
