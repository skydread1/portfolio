#:post{:id "pwd-manager-2FA"
       :page :blog
       :date ["2025-06-27"]
       :title "Password manager + 2FA"
       :css-class "pwd-man-2fa"
       :tags ["Bitwarden" "Password Manager" "2FA" "Security"]
       :image #:image{:src "/assets/pwd-man-2fa/blue-shield.png"
                      :src-dark "/assets/pwd-man-2fa/white-shield.png"
                      :alt "Bitwarden Logo"}}
+++
Using a password manager with strong passwords and enabling 2FA for critical accounts are simple yet powerful steps to protect your online security.
+++
## Rational

One of the best ways to store and organize your online credentials is to use a password manager.

I personally use Bitwarden because it is free, works on multiple devices, and has been around for a while. There are other options (both paid and free) such as Proton Pass and 1Password, to name a few.

Whether for your personal life or professional life, dealing with credentials is a task that is worth investing time into.

In this article, I will explain how a password manager works, how to use it properly, and why enabling 2FA is important for critical accounts.

This article is not sponsored in any way. I will use Bitwarden to illustrate the concepts, but they apply to other password managers as well.

## Password Manager

### Vault

A password manager contains one or more `vaults` that store your different credentials.

You can store your login credentials for the websites you’ve registered on, private notes, your credit card details, etc. Most password managers contain different `types` of credentials with the relevant fields:

![Bitwarden entry types](/assets/pwd-man-2fa/types.png)

Your vault is protected by a `master` password that you must remember.

### Password

Most importantly, the password manager allows you to quickly generate a random password (I often use passwords that are at least 30 characters long). So, when you register for a new website, you can generate a random password and add your email to the vault so you never have the same password in two different places:

![Bitwarden Password Generator](/assets/pwd-man-2fa/password.png)

So, more than just storing passwords, we generate them on the fly when we register somewhere. Of course, you can also use it to change your current password for a website.

### Adding a Credential Entry

Password managers such as Bitwarden have a browser extension that you can install to facilitate the creation of logins and allows you to auto-fill forms.

Let’s say you want to create a new account on [reddit.com](http://reddit.com/). Once on Reddit, you will typically click on your Bitwarden extension and click the `+` to add a new `login`. Bitwarden pre-fills some fields for you:

![Bitwarden Login tab](/assets/pwd-man-2fa/new-login.png)

The `item name` is just to identify what the login is for. You can rename it to just “Reddit” if you want, or if you have multiple reddit accounts, you can put “Reddit 1,” etc.

Then, you can add the username (which, depending on the website, can be a username or, most of the time, an email address).

And then you generate your password.

Furthermore, note that Bitwarden has also filled a `url` field with `https://www.reddit.com`. This is actually good because when you navigate to Reddit again, the extension icon will highlight that you have one login saved for this website, like so:

![Bitwarden extension icon](/assets/pwd-man-2fa/extension.png)

This way, you know that you are on the real website and not on a fake website like `redit.com` or a more subtle `ɾeddit.com`. Always verify that the little `1` in the extension is showing when you visit the website again.

Tou can also navigate to the website by clicking on the url field in you password manager which is convenient.

Finally, you can add custom fields.

### Protecting the Vault

We’ve seen that all the passwords in the vault should be randomly generated so nobody can guess them. But what about access to the vault itself?

To unlock the vault, you need the `master` password. It is the only password you need to remember.

Since this password unlocks your vault and therefore gives access to all your passwords, you must create a **strong password and remember it**. It is acceptable to write it down on a piece of paper and hide it (but never store it on your computer or in the cloud).

Also, I recommend enabling 2FA.

## 2FA

### What is 2FA?

> Two-factor authentication (2FA), also sometimes referred to as two-step verification or multi-factor authentication, is an extra layer of security that requires more than just a username and password to log into an account.
> 

Basically, you need another device to approve the login. Most websites dealing with critical logins, such as a password manager, will support and encourage 2FA.

> Two Factors: 2FA relies on two distinct factors to verify your identity.
> 
> - **Knowledge Factor**: This is usually your password, something you know.
> - **Possession Factor**: This is something you have, such as a code sent to your phone via SMS, a code generated by an authenticator app, or a security key.

It is advisable to use an **authenticator app**. There are multiple authenticator apps, such as Google Authenticator, Authy, Ante, etc.

### When to Use 2FA

A good rule of thumb is to enable 2FA for all your “critical” accounts, such as your email service, bank accounts, government accounts, etc. Of course, your password manager is the most critical account, so it’s better to have 2FA enabled for it.

### Generated Codes

Once you’ve entered your login and password on a website where you’ve enabled 2FA, you will be prompted to enter the 2FA code that is generated on your authenticator app on your phone. This code refreshes regularly. Once you enter the code, you are finally logged in.

### Recovery Codes

You will notice that when you activate 2FA on a website, it also allows you to save/print `recovery codes`.

> A recovery code provides an alternative method to verify your two-factor authentication if your authenticator app is not available. When you set up recovery codes, you get a list of codes that are unique to your login. Each code can be used once, and the system tracks each code as it is used.
> 

Basically, if you lose your phone, you still need a way to access your account, and recovery codes are there for that.

A common mistake is to store them alongside your password in your password manager. Some people just add a custom field like `2FA recovery` to their login entry and add the recovery code there. This is wrong because it defeats the purpose of 2FA. If you do that and someone accesses your vault, they can:

- Use the username + password to log in
- Use the 2FA recovery code to bypass your phone.

In other words, your 2FA is actually 1FA.

One could argue that if someone manages to access your vault and is determined, they could still access many accounts and cause lots of trouble. I think it’s better to be safe than sorry and limit the damage as much as possible.

To store your recovery codes, I think either of these two options is acceptable:

- Print them and store them in a safe place at your home (just label them to know which one is for which website).
- Store them in another dedicated vault (different from the one you use for your passwords).

### Forced 2FA

Some organizations, like your government or your bank, might “force” you to set up 2FA. So, you might already be using it in some places. You most likely have 2FA with OTP on your phone.

Some organizations have their own 2FA systems, often using their app. For instance, Steam uses Steam Guard, their own 2FA system. So, don’t be surprised if enabling 2FA differs from website to website.

## Emails

I recommend using 2FA for your emails. You can do that for Gmail, ProtonMail, etc.

Also, store your Gmail address passwords in your vault with randomly generated passwords, as usual.

### Different Email Addresses

There are many ways to manage your emails, but if you’re not yet using a password manager, there’s a high chance you use just a few Gmail addresses for your logins (or perhaps some Yahoo Mail or Hotmail from back in the MSN days!).

A simple solution would be to have multiple email addresses for different purposes. A minimal setup could be:

- A personal address (the one you give to friends, government, bank, etc.)
- A professional address (for job searches, clients, etc.)
- A trash address (for all the social media stuff)
- A shopping address (for all the stuff that contains your credit card info or where you make payments: hotels, trips, shopping, etc.)

### Get Rid of Old Addresses

If you have dormant Hotmail, Yahoo, etc., and you really don’t use them anymore, I suggest just deleting them.

If you think some people you know might contact you there, send them your new email address or create a redirect to your main mailbox.

### OAuth 2.0

> OAuth 2.0 is an open standard for authorization that allows users to grant third-party applications access to their resources on another service without sharing their credentials.
> 

Basically, you use OAuth 2.0 when you click on `Sign in with Google` or `Sign in with Facebook`, for instance.

The pros of using third-party logins are that you do not need to remember a separate password for each website. The problem with this setup is, what if your Google account is compromised?

What about your Facebook account being banned for some unknown reason? You would lose access to all the websites you logged in with Facebook. Convenience always has a cost, and the cost here is that you are reliant on Google and Facebook.

I recommend using email + password + 2FA login instead of third-party services like Google. `Privacy` folks will recommend this even more.

Of course, for more privacy, you would use a different email service than Gmail, but at least by using email + password, in the future, if you want to change your email address to, let’s say, a Proton address (or a custom domain one), it will be straightforward to do so on each website.

## Separation of Concerns

You might want to have multiple vaults. You could have one vault for personal logins and one vault for professional logins. You just need to remember two master passwords instead of one (DO NOT use the same one).

## It Seems Too Much Work Just for Login

You might think that, but it is the opposite.

Once the login is created (it only takes a few seconds to generate the password and enter your email address in the password manager), every time you go back to the website and your vault is unlocked, you can just auto-fill the forms. And if the website does not allow auto-fill, you can just copy/paste from the vault and you’re done.

Enabling 2FA will only be done on some of your accounts, and it does not take too long:

- Scan the QR code with your authenticator app and enter the generated code.
- Print recovery codes or add them to a secondary vault.

That does not take long to do and gives you peace of mind.

## Step-by-Step Recap

You want to create an account for a new bank.

1. Click the Bitwarden extension once on the website.
2. Add your email that you use for important stuff in the `username`.
3. Generate a random `password`.
4. Save and `auto-fill` the registration form for the bank.
5. Once the account is created, go to the settings, and you’ll see that you can enable `2FA` and click on that.
6. Open your authentication app (most likely Google Authenticator).
7. Scan the QR code shown by the bank to add the code to your app.
8. The website will expect you to enter the generated code to finalize the 2FA setup.
9. The bank will show the recovery codes (print them, label them, and hide them in your home).
