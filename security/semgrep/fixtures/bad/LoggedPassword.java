class LoggedPassword {
    void leak(org.slf4j.Logger log, User user) {
        log.info("password={}", user.getPassword());
    }

    static class User {
        String getPassword() {
            return "fixture";
        }
    }
}
