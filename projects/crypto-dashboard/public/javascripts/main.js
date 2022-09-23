$(function() {
    const $currencyRadio1 = $('#currencyRadio1');
    const $currencyRadio2 = $('#currencyRadio2');
    
    const $priceEl = $('#currentPrice');
    const $lastUpdateEl = $('#lastUpdateTs');
    const $priceChangePctEl = $('#priceChangePct');
    const $changeMinuteEl = $('#changeMinute');
    const REFRESH_TIME = 100;
    let currency = 'btc';
    let changeMinute = 1;
    let errorCount = 0;
    
    function toggleCurrency() {
        if($currencyRadio1.prop('checked')) {
            currency = 'btc';
        } else {
            currency = 'eth';       
        }
        $currencyRadio1.next().toggleClass('text-secondary text-primary');
        $currencyRadio2.next().toggleClass('text-secondary text-primary');
    }

    $currencyRadio1.change(toggleCurrency);
    $currencyRadio2.change(toggleCurrency);

    $changeMinuteEl.change(function(){
        let num = parseInt($changeMinuteEl.val());
        if(num < 1) {
            changeMinute = 1;
            $changeMinuteEl.val(1);
        } else if(num > 60) {
            changeMinute = 60;
            $changeMinuteEl.val(60);
        } else {
            changeMinute = num;
        }
    });

    function displayCurrentPrice(price) {
        $priceEl.html(price);
    }
    
    function displayLastUpdated(lastTimestamp) {
        $lastUpdateEl.html(moment(lastTimestamp + '+00:00').fromNow());
    }
    
    function displayPriceChangePct(prevPrice, currPrice) {
        const num = (currPrice - prevPrice) / prevPrice * 100;
        $priceChangePctEl.html(Math.round((num + Number.EPSILON) * 10000) / 10000);
        if(num > 0) {
            $priceChangePctEl.removeClass('text-danger');
            $priceChangePctEl.addClass('text-success');
        } else {
            $priceChangePctEl.removeClass('text-success');
            $priceChangePctEl.addClass('text-danger');
        }
    }
    
    async function refreshCurrentPrice() {
        try {
            const resp = await fetch(`/current-price/${currency}?timeDiff=${changeMinute}`);
            const data = await resp.json();
            console.log(data);
            if(data.length > 0) {
                displayCurrentPrice(data[0].current_price);
                displayLastUpdated(data[0].ts);
                displayPriceChangePct(data[0].first_price, data[0].current_price);
            }
            errorCount = 0;
        } catch(e) {
            console.error(e);
            errorCount++;
        }
    
        if(errorCount < 100) {
            timeoutHandle = setTimeout(()=> {
                refreshCurrentPrice();
            }, REFRESH_TIME);
        }
    }
    
    setTimeout(() => {
        refreshCurrentPrice();
    }, 100);
})
