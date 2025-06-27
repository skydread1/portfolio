#:post{:id "cd-sl-p-method"
       :page :blog
       :date ["2025-06-23"]
       :title "Custom Domain + SimpleLogin + Proton"
       :css-class "cd-sl-p"
       :tags ["ProtonMail" "SimpleLogin" "Custom Domain" "Privacy"]
       :image #:image{:src "/assets/cd-sl-p/proton-mail-icon.jpg"
                      :src-dark "/assets/cd-sl-p/proton-mail-icon.jpg"
                      :alt "ProtonMail Logo"}}
+++
CD-SL-P (Custom Domain + SimpleLogin + Proton) method to organize your email flow.
+++
## 👁️ Rationale

In this article, I will show you the CD-SL-P (Custom Domain + SimpleLogin + Proton) method that is good to balance privacy and convenience.

I will guide you through the steps of setting it up yourself, as well as explain the principles behind it.

This article assumes you have a Proton Unlimited Plan (which comes with SimpleLogin Premium), and it is not sponsored in any way. These are just tools I enjoy using.

You can use this method with the email/alias services of your choice; the principles remain the same.

### Reasons to move to this Email setup

#### Proton (Email Service)

I migrated from Gmail to Proton for the following reasons:

- I’d rather pay with money for an encrypted email service like Proton than pay Google with my personal data.
- Proton Unlimited comes with VPN, Drive (now with albums), SimpleLogin Premium (which I regularly use), and it also includes a password manager, etc.

#### SimpleLogin (Alias service)

SimpleLogin allows us to create aliases that forward emails to our mailboxes. In our case they forward emails to our proton mailboxes. It is a good way to deal with spam because you can create one alias per website you register with.

Therefore, if one website leaks your data (voluntarily or not) and you start getting spams in your Proton mailbox, you will be able to see, from the message headers, from which alias these spams came from.

You then know exactly which website leaked your address and you can block the alias you created for it. The website and spammers do not know your Proton address, making this setup easy to deal with spam.

#### Custom Domain

Proton helps with data privacy and SimpleLogin acts has a proxy between your Proton mailbox and the website you log into.

A custom domain helps you avoid being stuck with Proton or SimpleLogin if, for whatever reason, you want to change email/alias services.

A custom domain is a domain you buy with a Domain Registrar such as Cloudflare or PorkBun for instance. A domain looks like this for instance `gandalf.com`, `gandalf.me`, `gandalf.sg`. Depending on the extension, the price to rent the domain varies. If the domain is already bought by someone or a company, you can buy it from them. I advise sticking to trusted extensions such as `.com` (very expensive, often used for business), `.me` (good for personal email addresses and personal websites), and `.dev` / `.tech` if you are a software engineer. If you can, it might be a good idea to buy a few extensions for the same `label` (i.e. `gandalf.me` and `gandalf.pro`)

A few other good reasons I use custom domains are the following:

- it looks more professional in my opinion
- having multiple domains allows me to organize my emails better (more on that letter)
- it can be used for portfolio dev website
- I am not limited by already taken addresses when I want to add a new address to proton: if you own `@gandalf.me` you can create `contact@gandalf.me` (very clean) whereas gmail addresses such as `gandalf-contact@gmail.com` , `gandalf.contact@gmail.com` , or even`gandalfContact@gmail.com` (less clean) are less straight forward and also most likely taken by other people already.

## 👁️ Proton

### Proton additional addresses

With the Unlimited Plan, you can add up to 15 proton aliases (also called “additional addresses”, I believe to avoid confusion with SimpleLogin aliases). You can view these additional addresses as separate email addresses. However, note that they are all part of the same Proton account (the `proton.me` or `pm.me` address you used upon Proton account creation).

Therefore, you must set up 2FA and use a strong password for your Proton account, along with a reliable recovery method.

To create a new proton address, you need to find an unused label for the proton domains `.proton.me` or `pm.me`. So `shopping@pm.me` or `shopping@proton.me` will likely be unavailable, as they are probably already taken by someone else. Most likely `gandalf.shopping@pm.me` is taken too.

However, you can use up to 3 custom domains to Proton so we can happily setup the DNS records to use `gandalf.me` and `gandalf.pro` in Proton. Therefore, I can create email addresses such as:

- hi@gandalf.me
- shopping@gandalf.me
- contact@gandalf.pro
- research@gandalf.pro

And I am sure none of them are taken by others since I am the only one owning these domains! (hypothetically, I don’t own them, just an example)

### Compartimentation

To organize your emails in ProtonMail, you can use both `folders` and `labels`.

I personally use only `folders` as it is sufficient for my use case. You can create multiple folders in ProtonMail and drag and drop emails as they arrive in the proper folder. Here's an example of a folder structure that Gandalf could use:

```bash
├── me ## for gandalf.me domain
│   ├── hi ## for the hi label
│   └── shopping
├── pro ## for gandalf.pro domain
│   └── contact
│   └── research
```

In the example above, the folder structure mirrors the Proton aliases I mentioned earlier.

Of course, we don’t want to manually drag and drop every email and we can automate that process using `Filters`. Filters accept [Sieve scripts](https://proton.me/support/sieve-advanced-custom-filters).

Here is a Sieve script to move incoming emails to their respective folders:

```sieve
require ["fileinto", "imap4flags"];

if header :matches "X-Original-To" "hi@gandalf.me" {
  fileinto "me/hi";
} elsif header :matches "X-Original-To" "shopping@gandalf.me" {
  fileinto "me/shopping";
} elsif header :matches "X-Original-To" "contact@gandalf.pro" {
  fileinto "pro/contact";
} elsif header :matches "X-Original-To" "research@gandalf.pro" {
  fileinto "pro/research";
}
```

This script checks the headers of the email and move the email to the proper folder.

### Public Facing vs Private Facing

We often see these terms in DevOps to highlight what should remain internal (“hidden” from the outside) and what should be external (public).

A simple solution could be to give your proton address (or custom domain address used in Proton) to the website you want to register with. For instance, if Gandalf wants to register to `fireworks.xyz`, he could use `shopping@gandalf.me` as login. If he goes to `pipe-weed.express`, he can also use `shopping@gandalf.me` because it is related to the same activity and he wants to have them both under the same folder in his ProtonMail. 

To talk to his friend Galadriel, Gandalf will use `hi@gandalf.me`. 

This works fine. It resembles what many people do with their Gmail addresses: they split their emails into categories, one for personal life, one for professional life, and maybe one trash email for not-so-trusted sources or social media.

The downside of this setup is that all your Proton addresses are public-facing. We can trust Galadriel to not leak Gandalf email `hi@gandalf.me` but can we trust `fireworks.xyz`, what about `pipe-weed.express`? And if one of the 2 websites leak `shopping@gandalf.me`, how can we know who did it? Also, if the leak spreads, my `shopping@gandalf.me` address might become almost unusable unless I add a lot of filtering at the ProtonMail level to prevent all the spams.

The solution to this problem is to use an alias service, such as SimpleLogin.

## 👁️ SimpleLogin

### Rational

The solution to the problem above is to use SL as a proxy that forwards emails to the Proton addresses, thus hiding them from the outside.

SimpleLogin allows you to choose from its domains such as `@simplelogin.co` or `@mailaliases.com` .  The limitations of using SL domains instead of your own are the same as those highlighted for Proton. On top of that, SL domains are sometimes blacklisted by some websites.

SL allows you to use your custom domain to create aliases that you will give to all the websites you register with. I recommend using something a bit random such as `poTaToes.me` Websites do not care about how your domain looks as long as you can access it.

Without SL Premium, you cannot send emails from the alias; you can only reply to an email you received. With SL Premium, you can send an email from an alias but the mailbox features of SL are very limited.

Of course, it’s not possible to use the same custom domain in ProtonMail and SL because it doesn’t make sense, and it’s also not technically possible.

Finally, upon alias creation, you chose to which mailbox you want to forward emails to. You can change that anytime!

### Creating mailboxes

The first step is to add some `mailboxes` to your SL account. `mailboxe` is just the name of the address you want to forward to, in our case, the proton address. Once a `mailbox` added, it will be available in the list of emails you can forward messages to in the alias configuration.

### Creating aliases

To create an alias, the most convenient way is to use the SimpleLogin browser extension.

When you visit a website, clicking on the extension generate an alias with the name of the website with your custom domain such as `fireworks@poTatoes.me`.

Personally, I enable `Random Prefix Generation` in the settings, using `random combinations of 5 letters or digits` for the suffix generator.

This allows us to add extra characters to our generated alias, such as `fireworks.42tu7@poTatoes.me` . This is good practice because if one website leaks `reddit@poTaToes.me` , the spammer could easily guess what is our alias for Instagram (`instagram@poTaToes.me`), Facebook (`facebook@poTaToes.me`) and so on.

Let’s remember that our public facing domain will be given to possibly hundreds of website/news letters etc. We can be sure that one of our aliases will be leaked at some point, so having these 5 random digits will prevent spammers from guessing our other aliases.

### What to do in case of spam (scenario)

Gandalf created 2 aliases `fireworks.42tu7@poTatoes.me` and `pipe-weed.8eerf@poTatoes.me` and used them to register to the respective websites `fireworks.xyz` and `pip-weed.express`. Gandalf set `shopping@gandalf.me` as mailbox to forward to for both alias (remember that he wants all shopping related stuff under the same proton address). 

A few months pass and `shopping@gandalf.me` proton folder is bombarded with tons of unsolicited emails. Gandalf checks the header of the email and sees that the alias that forwarded all these emails is `pipe-weed.8eerf@poTatoes.me`. Thus, Gandalf knows that `pipe-weed.express` leaked his address and he can therefore disable the alias in SL (so he will stop receiving the spams). He can then log in to `pipe-weed.express` and change his email address to a new alias, such as `pipe-weed.abg65@poTatoes.me`. Also, he could just delete his account and move on with his life (if deleting his account his made impossible, at least the old alias has been disabled so no more spams).

You’ll notice that at no point was `shopping@gandalf.me` exposed.

### Changing mailboxes

What if we want to change the proton email we forward a specific alias to? We can just change it in the SimpleLogin dashboard and it takes effect immediately. Maybe Gandalf wants to have a dedicated proton address where he forwards all websites related to the parties he throws in the Shire: `party@gandalf.me`. Naturally he wants to forward `fireworks.xyz` emails to this new address. So he goes to SL and update the mailbox of the `fireworks.42tu7@poTatoes.me` alias and that’s it.

We can extend this to the extreme case where you switch email services! If you want to move from ProtonMail to another email service, you can simply change the mailbox address in SL. Instead of changing each alias one by one, you just change the mailbox from the proton mail address to the new email address and all affected aliases will start forwarding to the new address. Very convenient!

### When to avoid creating an alias

There are some cases where we might be willing to give our proton address instead. For example, to email your friends or family. I don’t think Galadriel will find it appropriate to be emailed by `friends.234rt@poTaToes.me`. In that case, Gandalf would use `hi@gandalf.me`. Also, it is not convenient to email from SL so if you need to message them first, you are stuck (especially without SL Premium). 

Another case could be for professional contacts. If Gandalf wants to start freelancing his fireworks shows, he might want to have a professional email address such as `contact@gandalf.pro` instead of something like `contact.show.gandalf.sdfgh@poTaToes.me`.

Another scenario could be for government related services.

In general, an address you need to give to people (not to use as login on a website) or that important entities might contact you from, I would recommend taking the risk to give your Proton address directly (therefore making it public-facing).

Without the Proton Unlimited Plan, so without being able to create additional addresses, I have to admit that it puts you in a taught spot but it is not the end of the world.

These are just recommendations of course.

## 👁️ Recap

Here is a recap of the setup to have a good visual overview:

![CD-SL-P Diagram example](/assets/cd-sl-p/example-diagram.png)

You can see that Gandalf uses its contact@gandalf.pro in his website for a professional look.

He also uses hi `hi@gandalf.me` address for his close friends.

For the rest, he relies on SimpleLogin aliases to hide his addresses.

The eye icons marks the public-facing addresses that have been exposed to the greater world (I wanted to make a Sauron eye joke but well)

## 👁️ Migrate from Gmail to CD-SL-P

This will take some time. The first thing to do is to go to all the websites you logged into before and request to change your email address to the new alias you created for that specific website.

In the case you use OAuth 2.0 (Login with Google/Facebook etc), you will often need to add your alias first has secondary email before being able to remove your google login from it.

A few websites do not allow to change address so you will need to delete the account and reopen a new one using your alias email this time.

You will need to tell your contacts that you changed your email as well (if you use a custom domain now, then that will be the last time you need to do that, let’s look on the bright side).

I advise you to update 5 to 10 accounts daily. You will see it is satisfying to see that list of aliases grow in your SL dashboards.

I advise to not delete the Gmail addresses too soon; wait for a year maybe to ensure you are not receiving anything anymore (meaning that all emails are properly going to Proton.

Finally, you might want to keep a few Gmail addresses for the accounts that could not have the email changed (and no easy to delete because of points/miles/rewards etc). Also, Gmail is still needed to use Google services so you will most likely need at least one Gmail address still (Drive, Photo etc).

## Conclusion

This setup is working fine for me and the transition was smooth. Some websites don’t make it easy to change email addresses, but ultimately, only a handful of websites didn’t allow me to change my email address.

This setup obviously comes at a cost (Custom domain cost + Proton Unlimited cost), but the services justify it in my opinion (VPN, SL Premium, Drive with 500GB of storage).
